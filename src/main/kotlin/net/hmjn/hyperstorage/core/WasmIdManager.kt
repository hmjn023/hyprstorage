package net.hmjn.hyperstorage.core

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.util.ItemHashUtil
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import java.io.IOException
import java.nio.file.Path

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
     * Save the ID mappings to a file.
     */
    fun save(filePath: Path) {
        val tag = CompoundTag()
        
        val itemsTag = CompoundTag()
        itemMapper.getItemMap().forEach { (name, id) ->
            itemsTag.putInt(name, id)
        }
        tag.put("Items", itemsTag)

        val nbtsTag = CompoundTag()
        itemMapper.getNbtMap().forEach { (hash, id) ->
            nbtsTag.putInt(hash.toString(), id)
        }
        tag.put("Nbts", nbtsTag)

        try {
            NbtIo.writeCompressed(tag, filePath)
        } catch (e: IOException) {
            Hyperstorage.LOGGER.error("Failed to save WasmIdManager mappings", e)
        }
    }
}
