# 2. Transport Logic & I/O Strategy

## 2.1. 輸送ロジック: "Logical Transport"

物理的な移動（BuildCraft型）を廃止し、**「論理輸送（瞬間移動）」**を採用することで計算量を極限まで削減する。

### Routing: O(1) Lookup
空間的な距離計算（マンハッタン距離）やグラフ探索（A*）を行わない。

*   **Channel-based:** アイテムは「チャンネル（周波数）」に対して送信される。
*   **Hash Map:** `Map<ChannelID, List<TargetNode>>` を保持。
*   **計算量:** 距離に関係なく、**O(1)** (定数時間) で転送先を特定する。

### Pub/Sub Model
*   **Publisher (Router):** アイテムを投げる側。「誰が受け取るか」を知る必要はない。
*   **Subscriber (Target):** 受け取る側。「このチャンネルのアイテムが欲しい」と登録する。
*   **Broker (Wasm):**
    *   **Filtering:** ビットマスク等を用いて、受信可能なノードを高速に抽出。
    *   **Load Balancing:** ラウンドロビン、最寄りの空き、ランダム等のロジックを数値計算のみで実行。

### Client-Side Prediction (不採用)
*   **方針:** サーバー（Wasm）からの結果通知のみを表示する **Server Authoritative** 方式。
*   **理由:** 瞬間移動型なので補間描画が不要。通信量と同期ズレのリスクを最小化する。

---

## 2.2. I/O 最適化: "The Last One Mile" Problem

内部（Wasm）がどれだけ速くても、外部ブロック（他Modの機械・チェスト）との物理I/O（`IItemHandler` 呼び出し）がボトルネックとなる。これを解決するための防衛策。

### Input Strategy (搬入)
*   **Ring Buffer:** Java側のImport Busにリングバッファ（一時保管所）を持たせる。
*   **Non-Blocking:** 外部からの `insertItem` はバッファに追記して即座に `return` する（遅延書き込み）。
*   **Batch Commit:** Tick終了時にバッファ内容をWasmへ一括転送（`memcpy`）する。

### Output Strategy (搬出)
*   **Speculative Bulk Insert (投機的実行):**
    *   通常: `simulate` (入るか確認) -> `execute` (実際に入れる) の2回呼び出し。
    *   最適化: いきなり `execute` でアイテムを押し込む。失敗した（余った）分だけをWasmに「返品」処理する。API呼び出しを半減させる。
*   **Smart Sleep (Back-off):**
    *   搬出先が満杯の場合、そのノードを「スリープ状態」にする。
    *   数秒間（または隣接ブロック更新まで）処理をスキップし、無駄なポーリングを停止する。
*   **Local Staging:** Export Bus自体に「1スタック分のキャッシュ」を持たせ、Wasmへの補充リクエスト頻度を下げる。

### Interface Strategy (外部連携)
外部Mod（パイプ等）からのアクセスに対する最適化。

*   **Cache / Proxy Inventory:**
    *   外部Modが `extractItem` を呼んだ際、Wasmへ問い合わせを行わない。
    *   InterfaceブロックがJavaヒープ上に持つ「キャッシュ用インベントリ（配列）」からアイテムを渡す。
    *   Wasmとの同期はTick末尾の一括処理のみ。
*   **Bloom Filter:**
    *   「このアイテムはあるか？」という問い合わせに対し、確率的データ構造で高速に「No」を返す。無駄な検索を回避する。

---

## 2.3. イベント駆動 vs ポーリング

Forge/Fabricには全ブロック対応のインベントリ変更通知フックがないため、ハイブリッドな検知を行う。

1.  **Internal (自Mod内):**
    *   `ItemStackHandler.onContentsChanged` をフックし、変更時のみ処理リスト（Dirty List）に追加する完全なイベント駆動。
2.  **External (他Mod):**
    *   **Comparator Update:** 隣接ブロックのコンパレーター更新を監視する。
    *   **Adaptive Polling:** 定期的に中身をチェックするが、変化がない場合はチェック間隔を延ばす（1tick -> 20tick -> 100tick）。

---

## 2.4. Wasmによる「物流の可視化」

物理移動は計算しないが、見た目上の楽しさを提供するため、**「GPUパーティクル」**による可視化を行う。

*   **Wasm:** アイテムの座標計算（始点→終点への補間）を大量に行い、座標データ配列（Float32Array）を生成。
*   **Java:** その配列を `ByteBuffer` として取得し、Sodium API等の **Instanced Rendering** に流し込む。
*   **効果:** 数千個のアイテムがパイプ内を流れる様子を、CPU負荷ほぼゼロで描画する。
