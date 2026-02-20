# 3. Data Model Definition (データモデル定義書) v1.1

## 3.1. Rust (Wasm) Data Structures
メモリ効率とキャッシュ効率を最大化するため、**完全な Structure of Arrays (SoA)** パターンを採用する。
構造体の配列 (AoS) は一切使用しない。

### 3.1.1. NetworkState
ネットワーク全体のデータを保持するルート構造体。

```rust
struct NetworkState {
    // --- Inventory (Storage) ---
    // インデックス i が同じ要素が1つのスロットを表す
    pub item_ids: Vec<u32>,        // ID (Interned Wasm ID)
    pub quantities: Vec<u64>,      // 数量 (u64::MAX)
    pub nbt_ids: Vec<u32>,         // NBT ID (0 = なし). ハッシュではない。
    
    // --- Channels (Transport) ---
    // Channel構造体は解体し、SoA化する
    // channel_id は index として扱う
    pub channel_subscribers: Vec<Vec<u32>>, // channel_id -> [node_id, node_id, ...]
    pub channel_filter_modes: Vec<bool>,    // true=White, false=Black
    pub channel_filter_ids: Vec<Vec<u32>>,  // channel_id -> [item_id, item_id, ...]
    
    // --- Nodes (Connection) ---
    // node_id は index として扱う
    pub node_types: Vec<NodeType>,
    pub node_priorities: Vec<i32>,
    pub node_buffers: Vec<Vec<(u32, u64)>>, // 一時バッファ
    
    // --- System ---
    pub tick_counter: u64,
    pub energy_buffer: u64,
}

enum NodeType {
    Storage = 0,
    Importer = 1,
    Exporter = 2,
    Interface = 3,
}
```

### 3.1.2. Memory Layout (Linear Memory)
Wasmリニアメモリの使用計画。

| 領域 | 用途 | 管理方法 |
| :--- | :--- | :--- |
| **0x0000 - 0x0FFF** | **Header & Stack** | Wasmシステム領域 |
| **0x1000 - 0xFFFF** | **Static Data** | 定数、文字列リテラル |
| **0x10000 - ...** | **Dynamic Heap** | `NetworkState` の可変長ベクタ (Slab Allocator) |
| **Fixed Offset** | **I/O Buffer** | Javaとの通信用固定バッファ (Double Buffering) |

---

## 3.2. Java Data Structures
Rust側と対になるデータ構造、およびマッピング情報。

### 3.2.1. WasmIdManager (Global ID & NBT Mapping)
文字列IDと数値IDの相互変換を行う**ワールド共有のシングルトン**。
全てのネットワーク（Wasmインスタンス）は、このクラスが発行した共通のID体系を使用する。これにより、ネットワーク統合時のIDリマッピングを不要にする。

```kotlin
object WasmIdManager {
    // RegistryName <-> ID (Global)
    private val stringToId = Object2IntOpenHashMap<String>()
    private val idToString = Int2ObjectOpenHashMap<String>()
    
    // NBT <-> ID (Global)
    // ハッシュ計算を行い、衝突時は `equals()` で比較して新しいIDを発行する
    // map: Hash -> List<{tag: CompoundTag, id: Int}>
    private val nbtCache = Long2ObjectOpenHashMap<List<NbtEntry>>()
    
    data class NbtEntry(val tag: CompoundTag, val id: Int)
    
    fun getItemId(res: ResourceLocation): Int
    fun getNbtId(tag: CompoundTag?): Int // 0 if null
    fun getNbtTag(id: Int): CompoundTag?
    
    // 永続化: world/hyperstorage/id_map.dat に保存
    fun save(dir: Path)
    fun load(dir: Path)
}
```

### 3.2.2. TransportPacket (Batch I/O)
Tick毎にWasmへ一括送信する構造体。

```java
class BatchInputPacket {
    int[] nodeIds;
    int[] itemIds;
    long[] quantities; // Java側でもlongだが、JS連携時はBigInt化に注意
    int[] nbtIds;      // ハッシュではなくID
}

class BatchOutputResult {
    int[] nodeIds;
    long[] remainingQuantities; // 挿入失敗した数（返品）
}
```

---

## 3.3. Persistence Format (永続化仕様)

### 3.3.1. Binary Dump
サーバー停止時に書き出すファイルフォーマット。`bincode` (Rust) の仕様に準拠。

*   **File Extension:** `.hvs` (Hyper-Visor Storage)
*   **Header:**
    *   `MAGIC`: "HVS\0" (4 bytes)
    *   `VERSION`: u32 (フォーマットバージョン)
    *   `TIMESTAMP`: u64
*   **Body:**
    *   `NetworkState` 構造体の生のバイト列 (圧縮済み)
*   **Footer:**
    *   `CHECKSUM`: CRC32

---

## 3.4. Scripting Data Model

### 3.4.1. JS Object Wrapper (Boa)
スクリプトから見えるオブジェクト。
**数値精度問題 (2^53制限) を回避するため、個数は `BigInt` を使用する。**

```typescript
// Network Object
interface Network {
    // Returns BigInt (e.g., 100n)
    getItemCount(itemId: string): bigint;
    
    setChannelActive(channelId: number, active: boolean): void;
    getEnergy(): bigint;
}

// Item Object (Read-only)
interface Item {
    id: string;
    count: bigint; // BigInt
    nbtHash: string;
}
```