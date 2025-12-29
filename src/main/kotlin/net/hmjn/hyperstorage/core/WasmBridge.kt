package net.hmjn.hyperstorage.core

import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import java.io.InputStream
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object WasmBridge {
    private val LOGGER: Logger = LogManager.getLogger("hyperstorage-wasm")
    private var instance: Instance? = null

    fun load(wasmStream: InputStream) {
        try {
            val module = Parser.parse(wasmStream)
            instance = Instance.builder(module).build()
            LOGGER.info("Wasm module loaded successfully.")

            // Initialize inventory system
            initInventory()
        } catch (e: Exception) {
            LOGGER.error("Failed to load Wasm module", e)
        }
    }

    // Test function
    fun add(a: Int, b: Int): Int {
        val inst = instance ?: return -1
        val addFn = inst.export("add")
        val result = addFn.apply(a.toLong(), b.toLong())
        return result[0].toInt()
    }

    // Initialize inventory system
    fun initInventory() {
        val inst = instance ?: return
        try {
            val initFn = inst.export("init_inventory")
            initFn.apply()
            LOGGER.debug("Inventory system initialized")
        } catch (e: Exception) {
            LOGGER.error("Failed to initialize inventory", e)
        }
    }

    // Add item to inventory
    // Returns the new total quantity for this item at this location
    fun addItem(itemId: Int, nbtHash: Long, quantity: Long, locationId: Int): Long {
        val inst = instance ?: return 0L
        try {
            val addItemFn = inst.export("add_item")
            val result = addItemFn.apply(itemId.toLong(), nbtHash, quantity, locationId.toLong())
            return result[0]
        } catch (e: Exception) {
            LOGGER.error("Failed to add item", e)
            return 0L
        }
    }

    // Remove item from inventory
    // Returns the remaining quantity (0 if all removed)
    fun removeItem(itemId: Int, nbtHash: Long, quantity: Long, locationId: Int): Long {
        val inst = instance ?: return 0L
        try {
            val removeItemFn = inst.export("remove_item")
            val result = removeItemFn.apply(itemId.toLong(), nbtHash, quantity, locationId.toLong())
            return result[0]
        } catch (e: Exception) {
            LOGGER.error("Failed to remove item", e)
            return 0L
        }
    }

    // Get total count of a specific item across all locations
    fun getItemCount(itemId: Int, nbtHash: Long): Long {
        val inst = instance ?: return 0L
        try {
            val getCountFn = inst.export("get_item_count")
            val result = getCountFn.apply(itemId.toLong(), nbtHash)
            return result[0]
        } catch (e: Exception) {
            LOGGER.error("Failed to get item count", e)
            return 0L
        }
    }

    // Get total count of items at a specific location
    fun getLocationCount(locationId: Int): Long {
        val inst = instance ?: return 0L
        try {
            val getCountFn = inst.export("get_location_count")
            val result = getCountFn.apply(locationId.toLong())
            return result[0]
        } catch (e: Exception) {
            LOGGER.error("Failed to get location count", e)
            return 0L
        }
    }

    // Get total number of unique item types
    fun getUniqueItemCount(): Int {
        val inst = instance ?: return 0
        try {
            val getCountFn = inst.export("get_unique_item_count")
            val result = getCountFn.apply()
            return result[0].toInt()
        } catch (e: Exception) {
            LOGGER.error("Failed to get unique item count", e)
            return 0
        }
    }

    // Clear all items at a specific location
    // Returns the number of item stacks removed
    fun clearLocation(locationId: Int): Int {
        val inst = instance ?: return 0
        try {
            val clearFn = inst.export("clear_location")
            val result = clearFn.apply(locationId.toLong())
            return result[0].toInt()
        } catch (e: Exception) {
            LOGGER.error("Failed to clear location", e)
            return 0
        }
    }
}
