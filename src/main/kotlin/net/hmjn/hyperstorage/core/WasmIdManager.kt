package net.hmjn.hyperstorage.core

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.util.ItemHashUtil
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.LevelResource
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.event.level.LevelEvent
import java.io.IOException
import java.nio.file.Path

/**
 * Global entry point for ID translation.
 * Delegates core mapping logic to WasmIdMapper.
 */
@EventBusSubscriber(modid = Hyperstorage.ID)
object WasmIdManager {
    private val itemMapper = WasmIdMapper()
    private val ID_MAP_FILE = "hyperstorage/id_map.dat"

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
            val file = filePath.toFile()
            if (file.parentFile != null) {
                file.parentFile.mkdirs()
            }
            NbtIo.writeCompressed(tag, filePath)
            Hyperstorage.LOGGER.debug("Saved WasmIdManager mappings to $filePath")
        } catch (e: IOException) {
            Hyperstorage.LOGGER.error("Failed to save WasmIdManager mappings", e)
        }
    }

    /**
     * Load the ID mappings from a file.
     */
    fun load(filePath: Path) {
        val file = filePath.toFile()
        if (!file.exists()) {
            resetForTesting() // Ensure clean state if no file
            return
        }

        try {
            val tag = NbtIo.readCompressed(filePath, NbtAccounter.unlimitedHeap()) ?: return

            val itemMap = mutableMapOf<String, Int>()
            val itemsTag = tag.getCompound("Items")
            for (key in itemsTag.allKeys) {
                itemMap[key] = itemsTag.getInt(key)
            }

            val nbtMap = mutableMapOf<Long, Int>()
            val nbtsTag = tag.getCompound("Nbts")
            for (key in nbtsTag.allKeys) {
                val hash = key.toLongOrNull() ?: continue
                nbtMap[hash] = nbtsTag.getInt(key)
            }

            itemMapper.loadData(itemMap, nbtMap)
            Hyperstorage.LOGGER.info("WasmIdManager loaded ${itemMap.size} item IDs and ${nbtMap.size} NBT IDs from $filePath")
        } catch (e: Exception) {
            Hyperstorage.LOGGER.error("Failed to load WasmIdManager mappings", e)
        }
    }

    /**
     * Reset the internal state. Used for testing and world changes.
     */
    fun resetForTesting() {
        itemMapper.reset()
    }

    @SubscribeEvent
    fun onLevelSave(event: LevelEvent.Save) {
        val level = event.level
        if (level is ServerLevel && level.dimension() == ServerLevel.OVERWORLD) {
            val path = level.server.getWorldPath(LevelResource.ROOT).resolve(ID_MAP_FILE)
            save(path)
        }
    }

    @SubscribeEvent
    fun onLevelLoad(event: LevelEvent.Load) {
        val level = event.level
        if (level is ServerLevel && level.dimension() == ServerLevel.OVERWORLD) {
            val path = level.server.getWorldPath(LevelResource.ROOT).resolve(ID_MAP_FILE)
            load(path)
        }
    }
}
