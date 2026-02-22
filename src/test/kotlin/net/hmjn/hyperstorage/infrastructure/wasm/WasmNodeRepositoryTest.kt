package net.hmjn.hyperstorage.infrastructure.wasm

import net.hmjn.hyperstorage.core.WasmBridge
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class WasmNodeRepositoryTest {
    private lateinit var client: ChicoryWasmClient
    private lateinit var repository: WasmNodeRepository

    @BeforeEach
    fun setup() {
        client = ChicoryWasmClient()
        client.addHostFunction(WasmBridge.createLogHostFunction())
        val wasmFile = File("src/main/rust/target/wasm32-unknown-unknown/release/hyper_visor_storage_wasm.wasm")
        client.load(wasmFile.inputStream())
        
        repository = WasmNodeRepository(client)
        repository.initNodeManager() // Start with a clean state before each test
    }

    @Test
    fun testNodeLifecycle() {
        // Register an Importer Node (0)
        val nodeId1 = repository.registerNode(10, 20, 30, 0)
        assertEquals(1L, nodeId1)

        // Register an Exporter Node (1)
        val nodeId2 = repository.registerNode(11, 21, 31, 1)
        assertEquals(2L, nodeId2)

        // Disable node 1 (inactive)
        repository.setNodeActive(nodeId1, 0)

        // Unregister node 1
        repository.unregisterNode(nodeId1)
    }
}
