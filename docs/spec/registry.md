# Registry & Resource Management (レジストリ・リソース管理)

## 概要 (Overview)
アイテム、液体、電力、ガスを Wasm 側で一律に扱うための「統合リソース管理」と、輸送の端点となる「ノード」の登録仕様を定義する。

---

## 統合リソース・レジストリ (Unified Resource Registry)

すべての移動可能リソースを `(Type, ID, MetadataHash)` の三つ組みで管理する。

### リソースタイプ (Resource Types)
| Value | Type | Unit |
| :--- | :--- | :--- |
| 0 | Item | count |
| 1 | Fluid | mB |
| 2 | Energy | FE / Joules |
| 3 | Gas | mB (Gas) |

---

## ノード・ライフサイクル管理 (Node Lifecycle)

Wasmが輸送計算を行うために、世界に存在する Importer/Exporter を Wasm 側にも「ノード」として登録する。

### ノード登録 (Node Registration)
1.  **BlockEntity Init:** `BlockEntity` の初期化時に Wasm の `register_node(pos, type)` を呼び出す。
2.  **NodeID Assignment:** Wasm は内部的な `NodeID` (u32) を発行し、Java 側の `BlockEntity` に保持させる。

### ノード削除 (Node Removal)
1.  **Block Broken:** `unregister_node(node_id)` を呼び出し、Wasm 側の SoA から該当ノードを削除。

---

## FFI シグネチャ (FFI Signatures)

Java (Host) から Wasm (Guest) へ公開・要求する主要な関数。

### ノード管理
- `register_node(x: i32, y: i32, z: i32, node_type: u8) -> u32`
    - 指定座標にノードを登録し、`NodeID` を返す。
    - `node_type`: 0=Importer, 1=Exporter
- `unregister_node(node_id: u32)`
    - ノードを削除し、関連する SoA データを整理する。
- `set_node_active(node_id: u32, active: u8)`
    - 1: Active, 0: Inactive (Chunk Unload/Redstone).

---

## 統合リソースレジストリの詳細

### ResourceID (u32)
- **0:** 未定義 / 空
- **1 ~ 4,294,967,295:** `WasmIdMapper` により割り振られた通し番号。
- **Persistence:** `WasmIdSavedData` により世界保存時に維持される。

### MetadataHash (u64)
- アイテムの `ComponentPatch` や液体の NBT を決定論的にハッシュ化した値。
- **Algorithm:** SipHash-2-4 または MurmurHash3。
- Wasm 側では 64bit の整数として O(1) で比較。

---

## Wasm SoA Layout (Lightweight Buffer)

Wasm 側では外部インベントリの全在庫を永続的に保持せず、**「ノード属性」と「移動待ちアイテム（インバウンドバッファ）」**のみを SoA で管理する。

- **NODE_TYPES: Vec<u8>**       // 0:Importer, 1:Exporter
- **NODE_CHANNELS: Vec<u32>**    // チャンネルID
- **NODE_PRIORITIES: Vec<i8>**   // 優先度
- **RESOURCE_IDS: Vec<u32>**     // バッファ内アイテムのID
- **METADATA_IDS: Vec<u32>**     // バッファ内アイテムのMetadata
- **BUFFER_QUANTITIES: Vec<u64>** // 配送待ちの数量
- **LOCATION_IDS: Vec<u32>**     // NodeID (物理座標との紐付け)
