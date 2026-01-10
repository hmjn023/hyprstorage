package net.hmjn.hyperstorage.core

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

/**
 * Pure mapping logic for Wasm IDs. Independent of Minecraft classes for testing.
 */
class WasmIdMapper {
    private val stringToId = Object2IntOpenHashMap<String>().apply { defaultReturnValue(0) }
    private val idToString = Int2ObjectOpenHashMap<String>()
    private var nextItemId = 1

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
}
