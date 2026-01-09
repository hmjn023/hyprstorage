# Hyper-Visor Storage Architecture V2: Comprehensive Design Specification

## 1. Project Overview & Philosophy

### 1.1. The "Hyper-Visor" Concept
本プロジェクトは、単なるMinecraft Modではなく、**Minecraftの物流・ストレージ処理を「仮想化（Hypervisor）」し、下層のハードウェア効率を極限まで引き出すためのシステム**である。
従来のModが抱えるパフォーマンス問題（TPS低下、ラグ）を、**Data-Oriented Design (データ指向設計)** と **WebAssembly (Wasm)** によって物理的に解決する。

*   **Brain (Guest/Wasm):** 思考・管理・計算を担当。Rustで実装。GCレス、リニアメモリ、SoAによるキャッシュ最適化。
*   **Body (Host/Java):** 物理世界との接点。Minecraft API (Block/Item/GUI) との連携。I/Oとレンダリングに専念する。

### 1.2. Why Wasm? (Technical Justification)
現代のMod環境におけるボトルネックは「メモリ容量」ではなく**「CPUのメモリ帯域・レイテンシ」**にある。

| 課題 | 従来 (Java / OOP) | 解決策 (Wasm / DoD) |
| :--- | :--- | :--- |
| **Pointer Chasing** | オブジェクトがヒープに散乱し、CPUキャッシュミスが多発。Stall（待ち時間）が処理の大半を占める。 | **Linear Memory & SoA:** データが連続配列に配置され、L1/L2キャッシュヒット率が劇的に向上する。 |
| **GC Pressure** | 計算のたびに短命オブジェクト（Iterator, Wrapper）が生成され、GCラグ（Stop-the-world）を誘発。 | **Zero Allocation:** 事前に確保されたメモリ領域を使い回すため、GCが一切発生しない。 |
| **Memory Footprint** | オブジェクトヘッダにより、int 1個でも数十バイト消費。 | **Raw Bytes:** `u32` は4バイト。メモリ効率が数倍〜数十倍。 |

---

## 2. Core Architecture: Rust & Wasm

### 2.1. Memory Layout: Structure of Arrays (SoA)
Wasm内では「アイテムオブジェクト」を持たず、プロパティごとの巨大な配列（Vector）として管理する。

```rust
struct NetworkState {
    // Hot Data (頻繁にアクセス・計算される)
    item_ids: Vec<u32>,        // 内部ID (Interned)
    quantities: Vec<u64>,      // 個数 (u64で21億個の壁を突破)
    location_ids: Vec<u32>,    // 所属する論理ノードID
    
    // Routing Data (輸送ロジック用)
    channel_ids: Vec<u32>,     // チャンネルID
    priorities: Vec<i32>,      // 優先度
}
```

### 2.2. Communication Strategy: Batching & Shared Memory
Java ↔ Wasm 間の呼び出し（Boundary Crossing）はコストが高いため、回数を最小限にする。

*   **No Fine-grained Calls:** `insertItem()` ごとにWasmを呼ばない。
*   **Batch Processing:** 変更があったデータはJava側でバッファリングし、Tickの終わりにまとめてWasmへ転送する。
*   **Direct Memory Access:** 可能な限り `DirectByteBuffer` を使用し、JavaとWasmでメモリ領域を共有してコピーコストを削減する（Chicory/Extism等の機能活用）。

---

## 3. Transport Logic: "The Router" (Logical Transport)

物理的なパイプ移動（BuildCraft型）を廃止し、**論理的な瞬間移動（EnderIO / Modular Routers型）**を採用する。

### 3.1. Routing Algorithm: O(1) Lookup
空間的な距離計算やグラフ探索（A*）を行わない。
*   **Channel-based:** アイテムは「チャンネル（周波数）」に送信される。
*   **Lookup Table:** `Map<ChannelID, List<TargetNode>>` を保持。送信時はリストを参照するだけで、距離に関係なく **O(1)** で宛先を特定する。
*   **Client-Side Prediction:** アイテム移動のアニメーションが必要な場合、サーバーは「発射」イベントのみを送り、クライアントが独自に補間描画する。

### 3.2. Pub/Sub Model
*   **Publisher (Sender):** チャンネルにアイテムを投げる。宛先の状態は気にしない。
*   **Subscriber (Receiver):** チャンネルを購読し、「欲しいアイテム（Filter）」や「優先度」を提示する。
*   **Broker (Wasm):**
    *   **Filtering:** ビットマスク等を用いて、受信可能なノードを高速に抽出。
    *   **Load Balancing:** ラウンドロビン、最寄りの空き、ランダム等のロジックを数値計算のみで実行。

---

## 4. I/O Optimization: "The Last One Mile" Problem

内部（Wasm）が爆速でも、外部ブロック（他Modの機械・チェスト）との物理I/Oがボトルネックとなる。これを解決するための防衛策。

### 4.1. Input Strategy (搬入)
*   **Ring Buffer:** Java側のImport Busにリングバッファ（一時保管所）を持たせる。
*   **Non-Blocking:** 外部からの `insertItem` はバッファに追記して即座に `return` する。
*   **Batch Commit:** Tick終了時にバッファ内容をWasmへ一括転送（`memcpy`）する。

### 4.2. Output Strategy (搬出)
*   **Speculative Bulk Insert (投機的実行):**
    *   シミュレーション（`simulate=true`）を行わず、いきなり本番実行（`simulate=false`）でアイテムを押し込む。
    *   失敗した（余った）分だけをWasmに「返品」する。これによりAPI呼び出し回数を半減させる。
*   **Smart Sleep (Back-off):**
    *   搬出先が満杯の場合、そのノードを「スリープ状態」にする。
    *   数秒間（または隣接ブロック更新まで）処理をスキップし、無駄なポーリングを停止する。
*   **Local Staging:** Export Bus自体に「1スタック分のキャッシュ」を持たせ、Wasmからの補充頻度を下げる。

### 4.3. Interface Strategy (外部連携)
*   **Cache / Proxy Inventory:**
    *   外部Modが `extractItem` を呼んだ際、Wasmへ問い合わせを行わない。
    *   InterfaceブロックがJavaヒープ上に持つ「キャッシュ用インベントリ（配列）」からアイテムを渡す。
    *   Wasmとの同期はTick末尾の一括処理のみ。
*   **Bloom Filter:** 「このアイテムはあるか？」という問い合わせに対し、確率的データ構造で高速に「No」を返し、無駄な検索を回避する。

---

## 5. Crafting System: Hierarchical Architecture

「単純作業」と「複雑作業」を分離し、AE2のエコシステムを利用しつつ計算負荷を取り除く。

### 5.1. Tier 1: Virtual Assembly (Internal / Wasm)
*   **対象:** 単純なレシピ（1:1変換、粉砕、圧縮）、中間素材。
*   **Mechanism:** 物理的なマシン動作やアイテム移動を行わない。Wasmメモリ上で `Input ID` の数値を減らし、`Output ID` の数値を増やすだけ。
*   **Performance:** 毎Tick数百万回のクラフトが可能。

### 5.2. Tier 2: Orchestration Mode (External / AE2 Slave)
*   **対象:** 複雑なNBT操作、AE2アドオン依存レシピ、特殊な機械（Mekanism等）。
*   **Role:** 本Modは「製造を行わない」。AE2を「下請け工場」として管理する。
*   **Micro-Orders (Just-In-Time):**
    *   巨大な注文（例: ソーラーパネル1000個）をそのままAE2に投げない（再帰計算ラグ防止）。
    *   Wasmが中間素材の在庫を監視し、不足分を小口でAE2に発注する。
*   **Promise / Token System:**
    *   外部機械に材料を投入後、Wasmは「完了トークン」を発行し、機械を忘れる（ポーリングしない）。
    *   製品が戻ってきた（Importされた）時点でトークンを消し込み、タスク完了とする。

---

## 6. Implementation Strategy

### 6.1. Event-Driven Updates (Dirty Flags)
*   **Internal:** 自Modのブロックは `ItemStackHandler.onContentsChanged` をフックし、変更時のみ処理リスト（Dirty List）に追加する。
*   **External:** Import/Export Busは「コンパレーター更新」を監視するか、適応的ポーリング（Adaptive Polling）を行う。

### 6.2. AE2 Integration Strategy
*   **Storage Bus Compatibility:** 本ModをAE2の「外部ストレージ」として認識させる。これによりAE2のGUIやオートクラフトから本Modの在庫を利用可能にする。
*   **Tag-based Routing:** Interfaceに「Crushing（粉砕）」などのタグを付け、Wasm側で動的にレシピの送り先（ロードバランシング）を決定する。これにより「機械のモード切替問題」を回避する。

### 6.3. Development Roadmap (Prioritized)

#### 【Phase 1: Foundation (Priority S)】
*   **Goal:** "Super Modular Routers" (ラグなし大量輸送)
*   Rust/Wasm ビルドパイプライン & Chicory 統合 (完了)
*   SoA データ構造 (完了)
*   **Next:** Pub/Sub 輸送ロジック & 無線ルーター実装
*   **Next:** I/O バッファリング & Last One Mile 最適化

#### 【Phase 2: Warehouse (Priority A)】
*   **Goal:** "Infinite Storage"
*   ID Mapping (完了)
*   仮想ストレージ・インターフェース (AE2連携の準備)
*   Wasm内 仮想クラフト

#### 【Phase 3: Integration (Priority B)】
*   **Goal:** "The Hyper-Visor"
*   AE2 API連携 (Slave Mode)
*   Orchestrator Logic (マイクロ発注システム)

---

## 7. Similar Technologies & References
*   **Factorio:** ベルトコンベアの内部処理（C++によるアイテムの一元管理・座標計算）。
*   **Sodium (Minecraft Mod):** レンダリングデータのバッファ化とゼロアロケーション。
*   **XNet / Modular Routers:** 輸送ロジックの概念的モデル（実装はWasmで置換）。
*   **Advanced Peripherals:** 外部からのAE2制御モデル。