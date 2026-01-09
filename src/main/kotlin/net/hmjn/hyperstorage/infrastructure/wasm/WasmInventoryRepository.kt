package net.hmjn.hyperstorage.infrastructure.wasm

import net.hmjn.hyperstorage.domain.repository.InventoryRepository

/**
 * Implementation of InventoryRepository using Wasm via ChicoryWasmClient.
 */
class WasmInventoryRepository(private val client: ChicoryWasmClient) : InventoryRepository {
    override fun addItem(itemId: Int, nbtHash: Long, quantity: Long, locationId: Int): Long {
        return client.callFunction("add_item", itemId.toLong(), nbtHash, quantity, locationId.toLong())
    }

    override fun removeItem(itemId: Int, nbtHash: Long, quantity: Long, locationId: Int): Long {
        return client.callFunction("remove_item", itemId.toLong(), nbtHash, quantity, locationId.toLong())
    }

    override fun getItemCount(itemId: Int, nbtHash: Long): Long {
        return client.callFunction("get_item_count", itemId.toLong(), nbtHash)
    }

    override fun getLocationCount(locationId: Int): Long {
        return client.callFunction("get_location_count", locationId.toLong())
    }

    override fun getUniqueItemCount(): Int {
        return client.callFunction("get_unique_item_count").toInt()
    }

    override fun clearLocation(locationId: Int): Int {
        return client.callFunction("clear_location", locationId.toLong()).toInt()
    }
}

