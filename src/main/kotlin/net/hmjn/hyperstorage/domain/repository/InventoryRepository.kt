package net.hmjn.hyperstorage.domain.repository

/**
 * Interface for inventory management operations.
 */
interface InventoryRepository {
    fun addItem(itemId: Int, nbtHash: Long, quantity: Long, locationId: Int): Long
    fun removeItem(itemId: Int, nbtHash: Long, quantity: Long, locationId: Int): Long
    fun getItemCount(itemId: Int, nbtHash: Long): Long
    fun getLocationCount(locationId: Int): Long
    fun getUniqueItemCount(): Int
    fun clearLocation(locationId: Int): Int
}
