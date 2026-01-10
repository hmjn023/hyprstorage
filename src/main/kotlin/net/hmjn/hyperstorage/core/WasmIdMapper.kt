package net.hmjn.hyperstorage.core

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Pure mapping logic for Wasm IDs. Independent of Minecraft classes for testing.
 * Uses a ReadWriteLock for high-concurrency access.
 */
class WasmIdMapper {
    private val lock = ReentrantReadWriteLock()
    
    // RegistryName <-> Item ID
    private val stringToId = Object2IntOpenHashMap<String>().apply { defaultReturnValue(0) }
    private val idToString = Int2ObjectOpenHashMap<String>()
    private var nextItemId = 1

    // NBT Hash <-> NBT ID
    private val hashToId = Long2IntOpenHashMap().apply { defaultReturnValue(0) }
    private val idToHash = Int2LongOpenHashMap().apply { defaultReturnValue(-1L) }
    private var nextNbtId = 1

    fun getIdForName(name: String): Int = lock.write {
        var id = stringToId.getInt(name)
        if (id == 0) {
            id = nextItemId++
            stringToId.put(name, id)
            idToString.put(id, name)
        }
        id
    }

    fun getNameForId(id: Int): String? = lock.read {
        idToString.get(id)
    }

    fun getNbtId(hash: Long): Int = lock.write {
        if (hash == 0L) return 0
        var id = hashToId.get(hash)
        if (id == 0) {
            id = nextNbtId++
            hashToId.put(hash, id)
            idToHash.put(id, hash)
        }
        id
    }

    fun getHashForNbtId(id: Int): Long = lock.read {
        if (id == 0) return 0L
        idToHash.get(id)
    }

    // --- Persistence Support ---

    fun getItemMap(): Map<String, Int> = lock.read {
        HashMap(stringToId)
    }

    fun getNbtMap(): Map<Long, Int> = lock.read {
        HashMap(hashToId)
    }

    fun loadData(items: Map<String, Int>, nbts: Map<Long, Int>) = lock.write {
        stringToId.clear()
        idToString.clear()
        stringToId.putAll(items)
        items.forEach { (name, id) -> idToString.put(id, name) }
        nextItemId = (items.values.maxOrNull() ?: 0) + 1

        hashToId.clear()
        idToHash.clear()
        hashToId.putAll(nbts)
        nbts.forEach { (hash, id) -> idToHash.put(id, hash) }
        nextNbtId = (nbts.values.maxOrNull() ?: 0) + 1
    }

    fun reset() = lock.write {
        stringToId.clear()
        idToString.clear()
        nextItemId = 1
        hashToId.clear()
        idToHash.clear()
        nextNbtId = 1
    }
}