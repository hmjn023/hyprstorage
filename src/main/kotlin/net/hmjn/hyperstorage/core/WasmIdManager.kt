package net.hmjn.hyperstorage.core

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.infrastructure.InventoryManager
import net.hmjn.hyperstorage.util.ItemHashUtil
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.event.level.LevelEvent

/**
 * Global entry point for ID translation.
 * Delegates core mapping logic to WasmIdMapper.
 */
object WasmIdManager {
    private val itemMapper = WasmIdMapper()

    /**
     * Get a Wasm ID for the given item stack.
     */
    fun getItemId(stack: ItemStack): Int {
        if (stack.isEmpty) return 0
        val res = BuiltInRegistries.ITEM.getKey(stack.item)
        return getIdForName(res.toString())
    }

    /**
     * Get a Wasm ID for the given registry name.
     */
    fun getItemId(res: ResourceLocation): Int {
        return itemMapper.getIdForName(res.toString())
    }

    /**
     * Get the registry name associated with a Wasm ID.
     */
    fun getName(id: Int): String? {
        return itemMapper.getNameForId(id)
    }

    /**
     * Get ID for a registry name string directly.
     */
    fun getIdForName(name: String): Int {
        return itemMapper.getIdForName(name)
    }

    /**
     * Get an NBT ID for the given stack's data components.
     */
    fun getNbtId(stack: ItemStack): Int {
        val hash = ItemHashUtil.getNbtHash(stack)
        return getNbtId(hash)
    }

    /**
     * Get an NBT ID for a pre-calculated hash.
     */
    fun getNbtId(hash: Long): Int {
        return itemMapper.getNbtId(hash)
    }

    /**
     * Get the hash associated with an NBT ID.
     */
    fun getNbtHash(id: Int): Long {
        return itemMapper.getHashForNbtId(id)
    }

    /**
     * Reset the internal state. Used for testing and world changes.
     */
    fun resetForTesting() {
        itemMapper.reset()
    }

    // Called manually via NeoForge.EVENT_BUS.addListener in Hyperstorage.kt
    internal fun onLevelSave(event: LevelEvent.Save) {
        val level = event.level
        if (level is ServerLevel && level.dimension() == ServerLevel.OVERWORLD) {
            val savedData = level.dataStorage.computeIfAbsent(WasmIdSavedData.factory(), WasmIdSavedData.FILE_NAME)

            savedData.itemMap = itemMapper.getItemMap()
            savedData.nbtMap = itemMapper.getNbtMap()
            savedData.wasmSnapshot = InventoryManager.getSnapshotBytes()

            savedData.setDirty()
            Hyperstorage.LOGGER.debug("Saved WasmIdManager mappings and Wasm snapshot to SavedData")
        }
    }

    internal fun onLevelLoad(event: LevelEvent.Load) {
        val level = event.level
        if (level is ServerLevel && level.dimension() == ServerLevel.OVERWORLD) {
            val savedData = level.dataStorage.computeIfAbsent(WasmIdSavedData.factory(), WasmIdSavedData.FILE_NAME)

            itemMapper.loadData(savedData.itemMap.toMutableMap(), savedData.nbtMap.toMutableMap())
            Hyperstorage.LOGGER.info(
                "WasmIdManager loaded ${savedData.itemMap.size} item IDs and ${savedData.nbtMap.size} NBT IDs from SavedData",
            )

            if (savedData.wasmSnapshot.isNotEmpty()) {
                val success = InventoryManager.restoreSnapshotBytes(savedData.wasmSnapshot)
                if (success) {
                    Hyperstorage.LOGGER.info("Wasm snapshot restored successfully (${savedData.wasmSnapshot.size} bytes).")
                } else {
                    // Provide fallback: we loaded IDs but Wasm failed. This is acceptable for simple cases where Wasm state will just gradually re-sync, but for hyperstorage it means lost inventory.
                    Hyperstorage.LOGGER.error("Failed to restore Wasm snapshot! In-memory inventory might be lost.")
                }
            } else {
                // Wasm loading happens in FMLCommonSetupEvent elsewhere, so just log it.
                Hyperstorage.LOGGER.info("No Wasm snapshot found (new world/first run).")
            }
        }
    }
}
