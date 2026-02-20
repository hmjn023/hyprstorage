# User Interface & Networking (UI・通信仕様)

## 概要 (Overview)
輸送ノード（Importer/Exporter）の設定を管理するための GUI および通信プロトコルを定義する。物理的なアイテム保持ではなく、Wasm 側の輸送ロジックを制御するためのインターフェースを提供する。

---

## ノード設定 GUI (Node Configuration Screen)

### 主要コンポーネント (Key Components)
1.  **Channel Input:** 0 ~ 999,999 のチャンネル番号を指定するテキストボックス。
2.  **Priority Slider/Buttons:** 配送優先度（-128 ~ 127）を設定。
3.  **Filter Slots (Ghost Slots):** アイテムを消費せずに登録するフィルター枠（9スロット）。
4.  **Mode Toggle:**
    *   **Active Mode:** 常時稼働 / レッドストーン制御 / パルス制御。
    *   **Whistlist / Blacklist:** フィルターの動作モード切替。

---

## フィルタリング仕様 (Filtering Specification)

### ゴーストスロット (Ghost Slots)
- アイテムをクリックしてもスロットに吸い込まれず、その `ItemStack` のコピーがアイコンとして表示される。
- 右クリックで登録解除。
- **Metadata Handling:** NBT（エンチャント等）を無視するかどうかのトグルスイッチを各スロットに配置。

---

## ネットワーク同期 (Network Synchronization)

NeoForge 1.21.1 の `CustomPacketPayload` を使用して、クライアント側の操作をサーバー側へ同期する。

### 設定更新パケット (NodeUpdatePayload)
クライアントからサーバーへ送信されるデータ構造（疑似コード）：
```kotlin
data class NodeUpdatePayload(
    val pos: BlockPos,
    val channel: Int,
    val priority: Int,
    val mode: Int,
    val filters: List<ItemStack>
)
```

### 通信フロー (Sync Flow)
1.  **Client:** GUI で値を変更（または「適用」ボタン押下）。
2.  **Client:** `NodeUpdatePayload` をサーバーへ送信。
3.  **Server:** 該当する `BlockEntity` を取得し、値を更新。
4.  **Server:** WasmBridge を介して Wasm 内の該当ノード（SoA）のデータを更新。
5.  **Server:** 周囲のクライアントへ同期パケットをブロードキャスト（必要な場合）。

---

## Wasm 連携フロー (Wasm Integration)

GUI での変更は、Wasm 側の `NODE_SOAs` に即座に反映される。

1.  **Channel Change:** `set_node_channel(node_id, new_channel)`
2.  **Priority Change:** `set_node_priority(node_id, new_priority)`
3.  **Filter Change:** Wasm 側にフィルターリスト（ItemID の配列）を転送し、`tick_transport` 時の判定に使用させる。

---

## ネットワーク同期 (Network Synchronization)

### パケット定義 (Packet Definition)
- **ID:** `hyperstorage:node_update` (CustomPacketPayload)
- **Direction:** Client -> Server (Settings), Server -> Client (Sync).

### 同期フロー (Hybrid Sync Flow)
1.  **Live Sync (テキスト):** チャンネル番号等のテキスト入力は、入力完了（Focus Lost または Enter）のタイミングでパケットを送信。
2.  **Immediate Sync (スロット):** ゴーストスロットへのアイテム登録・削除は、クリックの瞬間にサーバーへ送信。
3.  **Wasm Update:** サーバー側で受信後、即座に Wasm 側の `set_node_...` 関数を呼び出し、反映。

---

## フィルタリングの挙動 (Filtering Behavior)

- **Ghost Slots:** クライアント側でアイテムの「見た目（NBT 含む）」を保持。
- **Transfer:** Wasm 側へは `ResourceID` と `MetadataHash` のリストとして転送。
- **Metadata Toggle:** 各フィルター枠の横に「NBTを無視」するボタンを配置（1bit フラグとして Wasm へ送信）。

---

## 次のステップ (Next Implementation Steps)

- [ ] **Custom GUI Texture:** 汎用のディスペンサー GUI から、設定項目を配置しやすい独自テクスチャへの切り替え。
- [ ] **Packet Handler:** `CustomPacketPayload` の登録と受信処理。
- [ ] **Ghost Slot Logic:** `AbstractContainerMenu` におけるアイテム移動ロジックのオーバーライド。
