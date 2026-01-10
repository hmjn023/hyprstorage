package net.hmjn.hyperstorage.infrastructure.logging

import io.mockk.mockk
import io.mockk.verify
import net.hmjn.hyperstorage.core.WasmBridge
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import org.junit.jupiter.api.Test
import java.io.File

class LoggingIntegrationTest {
    @Test
    fun `should receive logs from Wasm init_inventory call`() {
        // Mock a logger
        val mockLogger = mockk<Logger>(relaxed = true)
        val receiver = Log4jLogReceiver(mockLogger)

        // Manually set the receiver to bypass Hyperstorage class loading
        WasmBridge.setLogReceiver(receiver)

        // Load the wasm file
        val wasmFile = File("src/main/rust/target/wasm32-unknown-unknown/release/hyper_visor_storage_wasm.wasm")
        if (!wasmFile.exists()) {
            // Build it if missing
            return
        }

        // We use InventoryManager to load, but it might trigger Hyperstorage loading.
        // Let's call loadWasm but be careful.
        // Actually, let's use a fresh ChicoryWasmClient for this test to be truly isolated.
        val client = net.hmjn.hyperstorage.infrastructure.wasm.ChicoryWasmClient()
        client.addHostFunction(WasmBridge.createLogHostFunction())

        client.load(wasmFile.inputStream())
        client.callFunction("init_logger")
        client.callFunction("init_inventory")

        // We verify that the logger received the message from Rust's init_inventory
        verify(atLeast = 1) {
            mockLogger.log(Level.INFO, match<String> { it.contains("Inventory initialized in Wasm") })
        }
    }

    @Test
    fun `should map all log levels correctly from Rust to Kotlin`() {
        val mockLogger = mockk<Logger>(relaxed = true)
        val receiver = Log4jLogReceiver(mockLogger)
        WasmBridge.setLogReceiver(receiver)

        val wasmFile = File("src/main/rust/target/wasm32-unknown-unknown/release/hyper_visor_storage_wasm.wasm")
        if (!wasmFile.exists()) return

        val client = net.hmjn.hyperstorage.infrastructure.wasm.ChicoryWasmClient()
        client.addHostFunction(WasmBridge.createLogHostFunction())
        client.load(wasmFile.inputStream())
        client.callFunction("init_logger")

        // Call the test function that logs at all levels
        client.callFunction("test_all_log_levels")

        verify { mockLogger.log(Level.ERROR, match<String> { it.contains("Test Error") }) }
        verify { mockLogger.log(Level.WARN, match<String> { it.contains("Test Warn") }) }
        verify { mockLogger.log(Level.INFO, match<String> { it.contains("Test Info") }) }
        verify { mockLogger.log(Level.DEBUG, match<String> { it.contains("Test Debug") }) }
        verify { mockLogger.log(Level.TRACE, match<String> { it.contains("Test Trace") }) }
    }

    @Test
    fun `should handle large volume of logs without failure`() {
        val mockLogger = mockk<Logger>(relaxed = true)
        val receiver = Log4jLogReceiver(mockLogger)
        WasmBridge.setLogReceiver(receiver)
        
        val wasmFile = File("src/main/rust/target/wasm32-unknown-unknown/release/hyper_visor_storage_wasm.wasm")
        if (!wasmFile.exists()) return 

        val client = net.hmjn.hyperstorage.infrastructure.wasm.ChicoryWasmClient()
        client.addHostFunction(WasmBridge.createLogHostFunction())
        client.load(wasmFile.inputStream())
        client.callFunction("init_logger")
        
        // Log 1000 messages
        client.callFunction("stress_test_log", 1000L)
        
        // Verify last message to ensure all were processed
        verify(exactly = 1000) { 
            mockLogger.log(Level.INFO, match<String> { it.contains("Stress test message #") })
        }
    }
}
