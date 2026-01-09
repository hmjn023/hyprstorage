package net.hmjn.hyperstorage.domain.model

/**
 * Domain model representing an item stack in the storage system.
 * Decoupled from Minecraft's ItemStack for core logic.
 */
data class ItemInfo(
    val itemId: Int,
    val nbtHash: Long,
    val count: Long
) {
    companion object {
        val EMPTY = ItemInfo(0, 0L, 0L)
    }

    fun isEmpty(): Boolean = count <= 0 || itemId == 0
}
