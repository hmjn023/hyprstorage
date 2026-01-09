package net.hmjn.hyperstorage.domain.service

import net.hmjn.hyperstorage.domain.model.ItemInfo
import net.hmjn.hyperstorage.domain.repository.InventoryRepository

/**
 * Domain service for inventory management.
 */
class InventoryService(private val repository: InventoryRepository) {
    /**
     * Synchronizes changes between current and previous inventory states.
     * Compares two lists of ItemInfo and updates the repository accordingly.
     */
    fun syncInventory(locationId: Int, current: List<ItemInfo>, previous: List<ItemInfo>) {
        val size = current.size.coerceAtMost(previous.size)
        for (i in 0 until size) {
            val curr = current[i]
            val prev = previous[i]

            if (curr != prev) {
                // 1. Remove previous item if it existed
                if (!prev.isEmpty()) {
                    repository.removeItem(prev.itemId, prev.nbtHash, prev.count, locationId)
                }

                // 2. Add new item if it exists
                if (!curr.isEmpty()) {
                    repository.addItem(curr.itemId, curr.nbtHash, curr.count, locationId)
                }
            }
        }
    }
}