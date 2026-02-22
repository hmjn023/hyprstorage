package net.hmjn.hyperstorage.network

import net.minecraft.core.GlobalPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData

/**
 * Stores a global mapping of custom node names to their GlobalPos.
 */
class NodeRegistrySavedData : SavedData() {
    private val nodeMap = mutableMapOf<String, GlobalPos>()

    fun registerNode(
        name: String,
        pos: GlobalPos,
    ) {
        if (name.isBlank()) return
        nodeMap[name] = pos
        setDirty()
    }

    fun removeNode(name: String) {
        if (nodeMap.remove(name) != null) {
            setDirty()
        }
    }

    fun removeNodeByPos(pos: GlobalPos) {
        val entry = nodeMap.entries.find { it.value == pos }
        if (entry != null) {
            nodeMap.remove(entry.key)
            setDirty()
        }
    }

    fun getNode(name: String): GlobalPos? {
        return nodeMap[name]
    }

    fun getAllNames(): List<String> {
        return nodeMap.keys.toList()
    }

    override fun save(
        tag: CompoundTag,
        registries: HolderLookup.Provider,
    ): CompoundTag {
        val listTag = ListTag()
        for ((name, pos) in nodeMap) {
            val entryTag = CompoundTag()
            entryTag.putString("Name", name)

            GlobalPos.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), pos)
                .resultOrPartial { err ->
                    // Log error if needed
                }
                .ifPresent { posTag ->
                    entryTag.put("Pos", posTag)
                }

            listTag.add(entryTag)
        }
        tag.put("Nodes", listTag)
        return tag
    }

    companion object {
        private const val DATA_NAME = "hyperstorage_nodes"

        fun load(
            tag: CompoundTag,
            registries: HolderLookup.Provider,
        ): NodeRegistrySavedData {
            val data = NodeRegistrySavedData()
            val listTag = tag.getList("Nodes", Tag.TAG_COMPOUND.toInt())
            for (i in 0 until listTag.size) {
                val entryTag = listTag.getCompound(i)
                val name = entryTag.getString("Name")
                if (entryTag.contains("Pos")) {
                    val posResult = GlobalPos.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), entryTag.get("Pos"))
                    posResult.resultOrPartial { }.ifPresent { pos ->
                        data.nodeMap[name] = pos
                    }
                }
            }
            return data
        }

        fun get(level: ServerLevel): NodeRegistrySavedData {
            val overworld = level.server.overworld()
            return overworld.dataStorage.computeIfAbsent(
                Factory(
                    { NodeRegistrySavedData() },
                    { tag, registries -> load(tag, registries) },
                ),
                DATA_NAME,
            )
        }
    }
}
