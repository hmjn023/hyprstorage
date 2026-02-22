package net.hmjn.hyperstorage.core

import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.saveddata.SavedData

class WasmIdSavedData : SavedData() {

    var itemMap: Map<String, Int> = emptyMap()
    var nbtMap: Map<Long, Int> = emptyMap()
    var wasmSnapshot: ByteArray = ByteArray(0)

    companion object {
        const val FILE_NAME = "hyperstorage_wasm_data"

        fun load(tag: CompoundTag, provider: HolderLookup.Provider): WasmIdSavedData {
            val data = WasmIdSavedData()

            val itemMap = mutableMapOf<String, Int>()
            val itemsTag = tag.getCompound("Items")
            for (key in itemsTag.allKeys) {
                itemMap[key] = itemsTag.getInt(key)
            }
            data.itemMap = itemMap

            val nbtMap = mutableMapOf<Long, Int>()
            val nbtsTag = tag.getCompound("Nbts")
            for (key in nbtsTag.allKeys) {
                val hash = key.toLongOrNull() ?: continue
                nbtMap[hash] = nbtsTag.getInt(key)
            }
            data.nbtMap = nbtMap

            if (tag.contains("WasmSnapshot")) {
                data.wasmSnapshot = tag.getByteArray("WasmSnapshot")
            }

            return data
        }

        fun factory(): Factory<WasmIdSavedData> {
            return Factory(::WasmIdSavedData, ::load, null)
        }
    }

    override fun save(tag: CompoundTag, provider: HolderLookup.Provider): CompoundTag {
        val itemsTag = CompoundTag()
        itemMap.forEach { (name, id) -> itemsTag.putInt(name, id) }
        tag.put("Items", itemsTag)

        val nbtsTag = CompoundTag()
        nbtMap.forEach { (hash, id) -> nbtsTag.putInt(hash.toString(), id) }
        tag.put("Nbts", nbtsTag)

        if (wasmSnapshot.isNotEmpty()) {
            tag.putByteArray("WasmSnapshot", wasmSnapshot)
        }

        return tag
    }
}
