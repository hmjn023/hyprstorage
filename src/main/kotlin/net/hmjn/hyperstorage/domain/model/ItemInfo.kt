package net.hmjn.hyperstorage.domain.model

import net.hmjn.hyperstorage.util.ItemHashUtil
import net.minecraft.world.item.ItemStack

/**
 * Domain model representing an item stack in the storage system.
 * Decoupled from Minecraft's ItemStack for core logic.
 */
data class ItemInfo(
    val itemId: Int,
    val nbtHash: Long,
    val count: Long,
) {
    companion object {
        val EMPTY = ItemInfo(0, 0L, 0L)

        fun fromItemStack(stack: ItemStack): ItemInfo {
            if (stack.isEmpty) return EMPTY
            return ItemInfo(
                itemId = ItemHashUtil.getItemId(stack),
                nbtHash = ItemHashUtil.getNbtHash(stack),
                count = stack.count.toLong(),
            )
        }
    }

    fun isEmpty(): Boolean = count <= 0 || itemId == 0
}
