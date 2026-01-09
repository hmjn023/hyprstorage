package net.hmjn.hyperstorage.infrastructure.wasm

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class WasmInventoryRepositoryTest {
    private lateinit var client: ChicoryWasmClient
    private lateinit var repository: WasmInventoryRepository

    @BeforeEach
    fun setup() {
        client = ChicoryWasmClient()
        val wasmFile = File("src/main/rust/target/wasm32-unknown-unknown/release/hyper_visor_storage_wasm.wasm")
        client.load(wasmFile.inputStream())
        client.callFunction("init_inventory")
        repository = WasmInventoryRepository(client)
    }

    @Test
    fun testAddItem() {
        // Add 10 items of type 1 at loc 1
        val result = repository.addItem(1, 100L, 10L, 1)
        assertEquals(10L, result)

        // Count it
        assertEquals(10L, repository.getItemCount(1, 100L))
    }
}
