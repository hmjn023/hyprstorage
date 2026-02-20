# Networking & Transport Logic (輸送・ネットワーク仕様)

## 概要 (Overview)
本Modの輸送システムは、物理的なアイテム移動（パイプ内の移動など）を一切計算せず、**論理的な接続（チャンネル）**を介した瞬間的なアイテム転送を行う。計算はすべてWasm内の整数演算で完結し、O(1)〜O(N)の極めて低い計算量で物流を制御する。

---

## チャンネル・アーキテクチャ (Channel Architecture)

### Pub/Sub モデル
*   **Publisher (Importer/Input):** 搬入ノード。外部インベントリからアイテムを取得し、特定の「チャンネルID」へ投げ込む。
*   **Subscriber (Exporter/Output):** 搬出ノード。特定の「チャンネルID」を購読し、フィルターに合致するアイテムを外部インベントリへ供給する。
*   **Broker (Wasm Core):** 全ノードの需給をマッチングし、最適な配送先を決定する。

### フィルタリング (Filtering)
各ノード（またはチャンネル）には以下のフィルターを設定可能。
*   **Item ID:** 文字列/数値IDによる一致。
*   **NBT Hash:** エンチャントや耐久値の厳密一致。
*   **Tags:** `forge:ingots/iron` 等のタグベースの一致。
*   **Priority:** 複数の搬出先がある場合の優先順位。

---

## ルーティング・アルゴリズム (Routing Algorithm)

### 配送優先順位
1.  **Priority (優先度):** 最も高い数値のノードへ優先的に送る。
2.  **Round-Robin (負荷分散):** 優先度が同じ場合、前回とは別のノードへ順番に送る。
3.  **Smart Sleep:** 搬出先が満杯の場合、Wasm側で当該ノードを計算対象から除外し、無駄なポーリングを停止する。

---

## 内部データ構造 (Internal Data Structures)

Wasm側では、キャッシュ効率を最大化するために **Structure of Arrays (SoA)** を用いてノードを管理する。

### Node SoA Layout
```rust
static mut NODE_TYPES: Vec<u8> = Vec::new();       // 0: Importer, 1: Exporter
static mut NODE_CHANNELS: Vec<u32> = Vec::new();   // チャンネルID
static mut NODE_PRIORITIES: Vec<i8> = Vec::new();  // 優先度 (-128 ~ 127)
static mut NODE_SLEEP_TICKS: Vec<u32> = Vec::new(); // 残りスリープ時間 (0ならアクティブ)
static mut NODE_BACKOFF_LVL: Vec<u8> = Vec::new(); // 指数バックオフの現在のレベル
```

### チャンネル・インデックス (Channel Index)
高速なマッチングのために、チャンネルIDからノードIDリストを引くハッシュマップを保持する。
`Map<ChannelID, (List<ImporterID>, List<ExporterID>)>`

---

## フィルタリングのバイナリ形式 (Filter Binary Format)

Java から Wasm に送られるフィルターデータのレイアウト（1 ノードあたり最大 9 スロット）。

| Size (bytes) | Type | Field | Description |
| :--- | :--- | :--- | :--- |
| 1 | u8 | MODE | 0: White, 1: Black, 2: Ignore |
| 1 | u8 | MATCH_NBT | 1: NBT一致必須, 0: ID一致のみ |
| 2 | u16 | COUNT | 有効なフィルター項目数 |
| 4*N | u32[] | RES_IDS | フィルター対象の ResourceID 配列 |
| 8*N | u64[] | META_IDS | フィルター対象の MetadataHash 配列 |

---

## 輸送バッファとオーバーフロー (Buffer & Overflow)

### Transfer Buffer
- **Max Size:** 1024 entries per tick (20ms).
- **Overflow Handling:** 
    - バッファが溢れた場合、Wasm 側で一時的に配送計算を中断し、フラグを立てる。
    - 次 tick で未処理分から優先的に再開。
    - **Backoff:** 頻繁に溢れる場合は `Smart Sleep` レベルを強制的に上昇させ、ノードの稼働率を下げる。

---

## Smart Sleep 状態遷移 (Smart Sleep State Machine)

搬出先（Exporter）が満杯でアイテムを受け入れられない場合、無駄な試行を減らすためにスリープ状態へ遷移する。

| レベル | スリープ時間 (Ticks) | 遷移条件 |
| :--- | :--- | :--- |
| 0 | 0 (Active) | 常に計算対象。転送成功でレベル維持。 |
| 1 | 10 (0.5s) | 転送失敗 (Full) |
| 2 | 40 (2.0s) | スリープ復帰後、即座に再失敗 |
| 3 | 200 (10s) | さらに連続失敗 |
| 4 | 1200 (60s) | 最大バックオフレベル |

*   **復帰:** 外部からインベントリが更新された通知（Java側からの `notify_update`）があった場合、強制的にレベル0へリセット可能。

---

## 輸送計算サイクル (Transport Cycle)

Wasm は「在庫の管理」ではなく、**「移動指示の発行」**に特化して毎 tick 動作する。

### 1. Supply Phase (Importer)
- Java 側が隣接インベントリから「引き抜ける」アイテムを Wasm の `BUFFER_QUANTITIES` へ Push する。
### 2. Matching Phase (Broker)
- Wasm 内で `BUFFER_QUANTITIES` (供給) と Exporter の `Demand` (需要/フィルター) をマッチング。
- 優先度 (Priority) と負荷分散 (Round-Robin) を適用。
### 3. Instruction Phase (Transfer Buffer)
- 移動が確定したアイテムを `Transfer Buffer` に書き出し、Java 側に「Exporter 隣接のインベントリへ挿入せよ」と指示を出す。
- **Atomic Operation:** 挿入に成功した分だけ、Wasm 側のバッファから差し引く。

### Transfer Buffer (共有メモリ)
大規模なアイテム移動が発生した場合、関数引数ではなく共有メモリ上のバッファを介して結果を Java 側へ通知する。

**Buffer Layout (per entry):**
1.  `source_node_id` (u32)
2.  `target_node_id` (u32)
3.  `item_id` (u32)
4.  `nbt_hash` (u64)
5.  `quantity` (u64)

---

## 次のステップ (Next Implementation Steps)

### Rust (Wasm) Logic
- [ ] **Node Registration:** ノードの追加・削除 API (`add_node`, `remove_node`)。
- [ ] **tick_transport:** Pub/Sub 配送処理の実装。
- [ ] **Channel Mapping:** チャンネルごとの高速検索インデックスの実装。

### Java (Host) Logic
- [ ] **Capability Provider:** 各ノードブロックにおける `IItemHandler` との連携。
- [ ] **Lazy Loading:** チャンクアンロード時に `NODE_SLEEP_TICKS` を最大値に設定し、計算を停止する。
- [ ] **Networking GUI:** Importer/Exporter 用のチャンネル設定・フィルター設定画面。
