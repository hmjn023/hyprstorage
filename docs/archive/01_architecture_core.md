# 1. Core Architecture & Wasm Foundation

## 1.1. 現状の課題とWasm導入の動機

### Minecraft Moddingにおけるボトルネック分析
現代のMod環境において、パフォーマンスの低下（TPS低下、ラグ）の主因は「メモリ容量不足」から**「CPUのメモリ帯域・レイテンシ（Cache Miss）」**へと移行している。

*   **Pointer Chasing (ポインタの追跡):**
    *   **現状:** Javaのオブジェクト指向モデル（AoS: Array of Structures）では、`List<ItemObject>` のようにデータがヒープメモリ上に散乱する。CPUは計算よりも「次のデータのアドレスをメモリから取ってくる時間（Stall）」に大半のクロックを費やしている。
    *   **解決策:** WasmのリニアメモリとRustの **SoA (Structure of Arrays)** を採用し、`item_ids: Vec<u32>`, `quantities: Vec<u64>` のようにデータを連続配置する。これによりL1/L2キャッシュヒット率を劇的に向上させる。

*   **Garbage Collection (GC) Pressure:**
    *   **現状:** 経路探索やインベントリ操作のたびに、一時的なラッパーオブジェクトやイテレータが大量に生成・破棄され、GC（Stop-the-world）を誘発する。
    *   **解決策:** Wasm内ではGCが発生しない（Rustの所有権モデル、または手動管理）。1億個のアイテムを管理しても、Javaからは「1つの巨大なバイト配列」に見えるため、GCスキャンの対象外となる。

### Wasm (WebAssembly) の採用理由
*   **Linear Memory:** Wasmのメモリはフラットなバイト配列であり、データ指向設計（DoD）を強制できる。
*   **Safety:** サンドボックス構造により、メモリエラーや暴走がホスト（Minecraftサーバー）を巻き込んでクラッシュさせるリスクを隔離できる。
*   **Polyglot:** Rustに限らず、C++やAssemblyScriptなど多様な言語でロジックを記述可能。

---

## 1.2. ランタイム構成 (Host vs Guest)

### Wasm Runtime: Chicory vs GraalVM
配布の容易さとパフォーマンスのバランスを取るため、**「ハイブリッド構成」**を採用する。

1.  **Chicory (Default):**
    *   **概要:** 100% Java実装のWasmランタイム。
    *   **メリット:** ネイティブ依存（JNI/.dll/.so）が一切ない。ModのJarファイルに同梱するだけで、あらゆるOS/アーキテクチャで動作する。
    *   **性能:** JITコンパイル（Wasm命令 -> Javaバイトコード）を行うため、Javaネイティブに近い速度が出る。メモリアクセスの局所性により、純粋なJavaオブジェクト操作より高速。

2.  **GraalWasm (Optional):**
    *   **概要:** GraalVMのPolyglot機能を使ったネイティブ実行。
    *   **メリット:** 世界最速クラスの実行速度。
    *   **運用:** サーバー管理者がGraalVMを導入している場合のみ、自動的にこちらに切り替える。クライアントに追加インストールを要求しない。

### データ構造: SoA (Structure of Arrays)
Rust側では、以下のような構造体を用いてデータを管理する。

```rust
struct NetworkState {
    // Hot Data (頻繁にアクセスされる)
    item_ids: Vec<u32>,        // ID (Interned)
    quantities: Vec<u64>,      // 個数 (u64で21億個制限を突破)
    
    // Routing Data
    connections: Vec<Vec<u32>>, // 接続グラフ
    priorities: Vec<i32>,       // 優先度
}
```

### 通信アーキテクチャ: Boundary Optimization
Java <-> Wasm 間の呼び出し（Boundary Crossing）には微小なオーバーヘッドがあるため、**「Batching（バッチ処理）」**を徹底する。

*   **Anti-Pattern:** `insertItem` が呼ばれるたびに Wasm関数を叩く。
*   **Best Practice:**
    1.  Java側にリングバッファを持たせ、変更を一時蓄積する。
    2.  Tickの終わりに、バッファの内容をまとめてWasmメモリへ転送（`memcpy` / `writeBytes`）する。
    3.  Wasm関数を1回だけ呼び出し、一括処理させる。

---

## 1.3. ID管理: String Interning

Wasm内部では文字列（Registry Name）を扱わず、全て整数（`u32`）で処理する。

*   **WasmIdManager (Java):**
    *   `Map<String, Integer>`: `minecraft:iron_ingot` -> `1`
    *   `Map<Integer, String>`: `1` -> `minecraft:iron_ingot`
*   **フロー:**
    1.  アイテム搬入時、Java側でRegistryNameをIDに変換。
    2.  Wasmには整数IDのみを渡す。
    3.  Wasm内での比較・検索はすべて整数演算（CPU 1クロック）で行う。
    4.  GUI表示やセーブ時のみ、文字列に戻す。

---

## 1.4. 安全性と永続化

### クラッシュ対策
*   **Isolation:** Wasmインスタンスがパニック（`unwrap()` 失敗など）しても、Javaプロセスは落ちない。`try-catch` で捕捉し、当該ネットワークのみを停止・再起動させる。
*   **Data Survival:** ロジック（Wasmインスタンス）が死んでも、データ（リニアメモリ/byte配列）はJavaオブジェクトとして残るため、データ消失は防げる。

### 永続化 (Persistence)
*   **Raw Binary Dump:** NBT形式（タグのツリー構造）への変換は遅すぎるため行わない。
*   **Shutdown Hook:** サーバー強制終了時、Wasmメモリをそのままバイナリファイルとしてディスクに書き出す。
*   **Format:** `bincode` (Rust) などの高速なバイナリ形式を使用。

### システム保護
*   **Memory Limit:** 1インスタンスあたりの最大メモリページ数を制限し、OSのメモリ枯渇（OOM）を防ぐ。
*   **Threading:** Wasm内はシングルスレッド厳守。Java側で `ExecutorService` を使い、ネットワーク単位で並列実行させる（マルチインスタンス）。
