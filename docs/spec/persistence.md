# Persistence (永続化仕様)

## 概要 (Overview)
Wasm内のデータ（IDマッピング、SoA在庫データ）が世界再起動後も正しく維持されるための保存・復元仕様を定義する。

---

## IDマッピングの永続化 (ID Mapping Persistence)

`WasmIdMapper` が保持するマッピング情報は、NeoForge の `LevelSavedData` を継承した `WasmIdSavedData` クラスで管理する。

### NBT 構造
```nbt
{
  "Resources": [
    { "Type": 0, "Name": "minecraft:stone", "ID": 1 },
    { "Type": 1, "Name": "minecraft:water", "ID": 2 }
  ],
  "Metadata": [
    { "Hash": 1234567890L, "ID": 1 }
  ],
  "NextResID": 3,
  "NextMetaID": 2
}
```

---

## Wasm在庫データのスナップショット (Wasm State Snapshot)

Wasm内の SoA (RESOURCE_IDS, QUANTITIES 等) は、Java側でループして NBT に変換すると低速なため、バイナリとして一括で保存する。

### 保存フロー (Binary Dump)
1.  **Trigger:** `LevelEvent.Save` 時に Wasm 側に `get_inventory_snapshot_size()` と `copy_inventory_to_buffer(ptr)` を呼び出し。
2.  **Storage:** Java 側で `ByteBuffer` を確保し、`memcpy` でデータを抽出。`byte[]` フィールドに格納して保存。

### 復元フロー (Restore)
1.  **Loading:** 世界ロード時に NBT から `byte[]` を読み込む。
2.  **Wasm Sync:** Wasm 側にメモリ確保を依頼し、`reconstruct_from_binary_dump(ptr, size)` を実行。

---

## Wasm在庫データのスナップショット (Wasm State Snapshot)

### バイナリ・レイアウト (Binary Layout)
バイナリデータの冒頭に以下のヘッダー（計 32 バイト）を付与する。

| Offset | Type | Field | Description |
| :--- | :--- | :--- | :--- |
| 0 | u32 | MAGIC | 0x48535031 ('HSP1') |
| 4 | u32 | VERSION | ファイルバージョン (1 = Initial) |
| 8 | u32 | LENGTH | 後続する SoA データ全体の長さ |
| 12 | u32 | CRC32 | データ全体のチェックサム |
| 16 | u64 | TIMESTAMP| 保存時刻 |
| 24 | u64 | RESERVED | 将来の拡張用 |

---

## エラーハンドリング (Error Handling)

### 不整合時の救済措置 (Corruption Recovery)
1.  **CRC不一致:** ロードを中止し、バックアップファイル（`.bak`）の読み込みを試行。
2.  **ID不一致:** `WasmIdMapper` に存在しない ID がバイナリに含まれている場合、該当リソースを `minecraft:air` に変換し、警告ログを出力。
3.  **座標消失:** `NodeID` に対応するブロックが世界に存在しない場合、その在庫を安全な場所に「紛失物」として退避。

---

## 次のステップ (Next Steps)
- [ ] `WasmIdSavedData` の実装。
- [ ] Wasm (Rust) 側での SoA データ・シリアライズ機能の追加。
