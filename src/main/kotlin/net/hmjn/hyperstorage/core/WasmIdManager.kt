package net.hmjn.hyperstorage.core

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

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
        return getItemId(res)
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
}
