package net.hmjn.hyperstorage.infrastructure.wasm

import net.hmjn.hyperstorage.core.WasmBridge
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class ChicoryWasmClientTest {
    @Test
    fun testAddFunction() {
        val client = ChicoryWasmClient()
        client.addHostFunction(WasmBridge.createLogHostFunction())

        // Load the compiled wasm file from the rust target directory
        val wasmFile = File("src/main/rust/target/wasm32-unknown-unknown/release/hyper_visor_storage_wasm.wasm")
        if (!wasmFile.exists()) {
            // Fallback to build artifacts if not in source
            // Note: In real CI/build, this should be better managed
        }

        client.load(wasmFile.inputStream())

        // Call the 'add' function: 10 + 20 = 30
        val result = client.callFunction("add", 10L, 20L)

        assertEquals(30L, result, "Wasm 'add' function should return 30")
    }
}
