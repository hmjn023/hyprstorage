package net.hmjn.hyperstorage.infrastructure

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.domain.repository.InventoryRepository
import net.hmjn.hyperstorage.domain.service.InventoryService
import net.hmjn.hyperstorage.infrastructure.wasm.ChicoryWasmClient
import net.hmjn.hyperstorage.infrastructure.wasm.WasmInventoryRepository
import java.io.InputStream

/**
 * Singleton manager for storage-related services and infrastructure.
 */
object InventoryManager {
    private val wasmClient = ChicoryWasmClient()
    private val repository: InventoryRepository = WasmInventoryRepository(wasmClient)
    private val service = InventoryService(repository)

    fun loadWasm(wasmStream: InputStream) {
        try {
            wasmClient.load(wasmStream)
            wasmClient.callFunction("init_inventory")
            Hyperstorage.LOGGER.info("Wasm module loaded and inventory initialized via InventoryManager.")
        } catch (e: Exception) {
            Hyperstorage.LOGGER.error("Failed to load Wasm in InventoryManager", e)
        }
    }

    fun getInventoryService(): InventoryService = service

    // Low level access if needed (optional)
    fun getInventoryRepository(): InventoryRepository = repository
}
