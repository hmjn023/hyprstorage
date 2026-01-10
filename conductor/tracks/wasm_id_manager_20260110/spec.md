# Specification: ID Translation Layer (WasmIdManager)

## 1. Overview
このトラックでは、Minecraft のアイテムレジストリ名（`ResourceLocation`）と、Wasm 内部で使用する数値 ID（`u32`）の相互変換を行う `WasmIdManager` を実装します。また、NBT データをハッシュ化して数値 ID にマッピングする機能も含みます。これにより、Wasm 側でメモリ効率の良い SoA データ構造を用いたアイテム管理が可能になります。

## 2. Functional Requirements
### 2.1 Item ID Mapping
- **Bi-directional Mapping:** `ResourceLocation` (String) と `Int` ID の双方向変換をサポートします。
- **On-demand Assignment:** 未登録のアイテムがシステムに投入された際、動的に新しい ID を発行します。
- **Global Consistency:** ワールド内のすべての Wasm インスタンスで共通の ID 体系を使用します。

### 2.2 NBT ID Mapping
- **NBT to ID:** `CompoundTag` をハッシュ化し、衝突時は `equals()` で比較して一意な ID を発行します。
- **Zero for Null:** NBT が存在しない場合は ID `0` を割り当てます。
- **Tag Retrieval:** ID から元の `CompoundTag` を復元できる機能を保持します。

### 2.3 Persistence
- **Storage Path:** `world/hyperstorage/id_map.dat` にマッピング情報を保存します。
- **Format:** Minecraft 標準の **NBT 形式** を採用し、データの堅牢性を確保します。
- **Lifecycle:** ワールドの保存時に自動的に書き出し、ロード時に復元します。

## 3. Technical Implementation
- **Data Structures:** 高速な検索のため、`fastutil` の `Object2IntOpenHashMap` および `Int2ObjectOpenHashMap` を使用します。
- **Concurrency:** 複数の Wasm インスタンスやスレッドからの同時アクセスを考慮し、スレッドセーフな実装を行います。
- **Integration:** 既存の `ItemHashUtil` を利用して NBT のハッシュ計算を行います。

## 4. Acceptance Criteria
- [ ] アイテム `minecraft:iron_ingot` を投入した際、一意な ID が発行され、その ID から元の名前が復元できること。
- [ ] 同じ内容の NBT を持つアイテムに対して、常に同じ NBT ID が返されること。
- [ ] サーバーを再起動した後も、アイテム名と ID の対応関係が維持されていること。
- [ ] 未登録のアイテムに対して ID を取得しようとした際、自動的に新規 ID が割り当てられること。

## 5. Out of Scope
- Wasm 内部での ID 変換処理（変換は常に Java/Kotlin 側で行います）。
- アイテムタグ（`#minecraft:logs` 等）の数値 ID 化（将来のトラックで検討）。
