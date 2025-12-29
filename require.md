# High-Performance Wasm Logistics Mod: "Hyper-Visor Storage" Final Design Specification

## 1. プロジェクト概要 & コアコンセプト
Rust (WebAssembly) と Java のハイブリッド構成による、Minecraft (Forge/NeoForge/Fabric) 用の次世代物流・倉庫Mod。
既存Modのボトルネック（GCラグ、経路探索負荷、NBT操作コスト）を、Wasmのリニアメモリとデータ指向設計（SoA）によって完全に排除する。

### 設計哲学
1.  **Brain (Wasm):** 思考・管理・計算を担当。Rustで実装。GCレス、連続メモリ、SoAによるキャッシュ最適化。
2.  **Body (Java):** 物理干渉・I/O・描画を担当。Minecraft APIとの丁寧な連携。
3.  **No Physics:** パイプ内のアイテム描画や物理移動は行わない。EnderIO型の「論理接続」とModular Routers型の「無線転送」を採用。

---

## 2. 技術スタック (Tech Stack)

### 言語 & ビルド
* **Host:** Java 17/21 (Minecraft Versionによる)
* **Guest:** Rust (Edition 2021) -> Target: `wasm32-unknown-unknown`
* **Build System:** Gradle (Polyglot構成)
    * Gradleタスク内で `cargo build --release` を実行。
    * 生成された `.wasm` を `src/main/resources/wasm/` に自動配置。

### ライブラリ & ランタイム
* **Wasm Runtime (Java):**
    * **Primary (Default):** `Chicory` (Pure Java, 依存関係ゼロ, 配布用)
    * **Optional (High-Perf):** `GraalVM Polyglot` (サーバー環境がGraalVMの場合のみ自動有効化)
* **JS Engine (Rust):** `Boa` (Wasmバイナリに内包。ユーザーフィルタリング用)
* **Serialization (Rust):** `bincode` (高速なバイナリシリアライズ)

---

## 3. アーキテクチャ詳細設計

### 3.1. メモリ管理 & データ構造 (SoA)
Javaのオブジェクト指向（AoS）を廃止し、Rust側では **Structure of Arrays (SoA)** を徹底する。

* **Item Identification:** Wasm内ではNBTを扱わず、`ItemKey { id: u32, nbt_hash: u64 }` のペアでアイテムを識別する。
* **Rust (Internal State):**
    ```rust
    struct NetworkState {
        item_ids: Vec<u32>,        // アイテム定義ID (String Interning済み)
        nbt_hashes: Vec<u64>,      // NBTの決定論的ハッシュ
        quantities: Vec<u64>,      // 在庫数
        location_ids: Vec<u32>,    // 物理/論理的な格納場所
    }
    ```
* **Java (NBT Dictionary):** Java側で `Map<Long, CompoundTag>` を保持し、Wasmから返されたハッシュを元にNBTを復元する。

### 3.2. IDマッピング (The Translation Layer)
* **Java側:** `WasmIdManager` (Singleton)
    * `Map<String (RegistryName), Integer (WasmID)>` の双方向マップを保持。
    * 起動時およびセーブデータロード時に動的にIDを割り振り、Wasmへテーブルを同期する。

### 3.3. 並列化 & スレッド戦略
* **Multi-Instance Sharding:** 輸送ネットワークIDごとに独立したWasmインスタンス（Memory空間）を生成する。
* **Java Threading:** Java側のスレッドプール（`ExecutorService`）を使用し、各Wasmインスタンスを別スレッドで並列計算させる。
* **Locking:** `ReadWriteLock` を使用。計算（Write）中はロック、参照（Read/GUI表示用データ取得）は並列可とする。

---

## 4. 機能要件 (Feature Specifications)

### 4.1. 輸送ロジック (Pub/Sub Model)
* **Instant Transfer:** 座標距離計算を排除。`Channel ID` に基づくハッシュマップ参照 (`O(1)`)。
* **Dirty Flag:** 在庫変動があったノードのみを計算対象リストに追加し、毎Tickの全走査を回避する。

### 4.2. 外部Mod連携 & I/O 最適化
* **Buffered I/O (Staging Area):**
    * Input: Java側でリングバッファにアイテムを溜め、Tick終了時に `memcpy` でWasmメモリへ一括転送。
    * Output: 「投機的インサート」を採用。シミュレーションなしで外部インベントリへ突っ込み、失敗分のみWasmへ返品する。
* **Smart Sleep:** 出力先が満杯の場合、当該ノードを数秒間「スリープ」させ、無駄なI/Oチェックを停止する。

### 4.3. クラフト機能 (Hybrid Logic)
* **Tier 1 (Wasm Internal):** 単純な合成、粉砕等はWasm内のグラフで即座に完了させる。
* **Tier 2 (AE2 Slave Mode):** 複雑なレシピは、WasmがAE2の `ICraftingGrid` に対し「中間素材」を小口発注することで、AE2側の計算ラグ（再帰計算）を回避しつつエコシステムを利用する。

### 4.4. スクリプト機能 (Programmable Filter)
* **JS Integration:** `Boa` エンジンにより、ユーザーがJavaScript (ES6) で高度なフィルタリング・優先度制御を記述可能。
* **Safety:** 無限ループ対策として Wasmランタイムの `Instruction Metering` (Fuel) を有効化（1000〜10000命令程度）。

---

## 5. 安全性 & 永続化 (Reliability)

### 5.1. クラッシュ対策 & 整合性
* **Double Buffering:** Wasmメモリを「現行(Current)」と「次期(Next)」に分け、計算完了時のみポインタを切り替える。
* **Circuit Breaker:** Wasm実行部は `try-catch` で保護。パニック時は当該ネットワークのみ「停止」させ、サーバー全体のクラッシュを防ぐ。

### 5.2. データ永続化 (Persistence)
* **Format:** NBT変換を介さず、Wasmリニアメモリ (`byte[]`) をそのまま **Raw Binary Dump** してディスクに保存。
* **Emergency Save:** `Shutdown Hook` を登録し、JVM強制終了時（Watchdogキル等）にメモリ内容を緊急ダンプする。

---

## 6. 実装ロードマップ & ディレクトリ構成

### プロジェクト構造
```text
MyMod/
├── build.gradle          # Rustビルドタスク, dependencies: chicory, graal-sdk
├── src/main/java/core    # IWasmRuntime, Chicory/Graal Adapters
├── src/main/java/network # WasmIdManager, PacketHandler
├── src/main/java/block   # Buffered I/O (TileEntities)
├── src/main/resources/wasm/ # .wasm 排出先
└── src/main/rust         # Rust Sources (SoA, Boa, Bincode)
```

### 開発フェーズ
1.  **Phase 1:** Gradleビルドパイプライン構築。Chicory/GraalVM 両対応の `IWasmRuntime` アダプター実装。
2.  **Phase 2:** `WasmIdManager` と NBT Hashing 実装。Wasm側 SoA 在庫管理の基本実装。
3.  **Phase 3:** 無線輸送ロジックとバッファ付き I/O システムの構築。
4.  **Phase 4:** Boa JSエンジンの統合と、AE2スレーブモード連携。
5.  **Phase 5:** シャットダウンフックとダブルバッファリングによる堅牢化。