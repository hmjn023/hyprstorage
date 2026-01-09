package net.hmjn.hyperstorage.core

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.ItemStack

/**
 * Manages the mapping between Matrix Item Registry Names (String) and Wasm IDs (Int). Ensures
 * collision-free IDs for Wasm processing.
 *
 * TODO: Implement save/load logic to persist ID mappings across server restarts.
 */
object WasmIdManager {
    private val idToName = ArrayList<String>()
    private val nameToId = HashMap<String, Int>()

    /**
     * Get a deterministic Wasm ID for the given item stack. Assigns a new ID if one doesn't exist.
     */
    fun getId(stack: ItemStack): Int {
        if (stack.isEmpty) return 0

        val registryName = BuiltInRegistries.ITEM.getKey(stack.item).toString()
        return getIdForName(registryName)
    }

    /** Get ID for a registry name string. */
    @Synchronized
    fun getIdForName(name: String): Int {
        return nameToId.computeIfAbsent(name) {
            val newId = idToName.size + 1 // Start IDs at 1 (0 is empty/null)
            idToName.add(name)
            newId
        }
    }

    /** Get the registry name associated with a Wasm ID. */
    @Synchronized
    fun getName(id: Int): String? {
        if (id <= 0 || id > idToName.size) return null
        return idToName[id - 1]
    }
}
