package net.hmjn.hyperstorage.core

import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

/**
 * Pure mapping logic for Wasm IDs. Independent of Minecraft classes for testing.
 */
class WasmIdMapper {
    // RegistryName <-> Item ID
    private val stringToId = Object2IntOpenHashMap<String>().apply { defaultReturnValue(0) }
    private val idToString = Int2ObjectOpenHashMap<String>()
    private var nextItemId = 1

    // NBT Hash <-> NBT ID
    private val hashToId = Long2IntOpenHashMap().apply { defaultReturnValue(0) }
    private val idToHash = Int2LongOpenHashMap().apply { defaultReturnValue(-1L) }
    private var nextNbtId = 1

    @Synchronized
    fun getIdForName(name: String): Int {
        var id = stringToId.getInt(name)
        if (id == 0) {
            id = nextItemId++
            stringToId.put(name, id)
            idToString.put(id, name)
        }
        return id
    }

    @Synchronized
    fun getNameForId(id: Int): String? {
        return idToString.get(id)
    }

    @Synchronized
    fun getNbtId(hash: Long): Int {
        if (hash == 0L) return 0
        var id = hashToId.get(hash)
        if (id == 0) {
            id = nextNbtId++
            hashToId.put(hash, id)
            idToHash.put(id, hash)
        }
        return id
    }

    @Synchronized
    fun getHashForNbtId(id: Int): Long {
        if (id == 0) return 0L
        return idToHash.get(id)
    }
}