package net.hmjn.hyperstorage.core

import io.mockk.mockk
import net.hmjn.hyperstorage.infrastructure.logging.LogReceiver
import org.junit.jupiter.api.Test

class WasmBridgeTest {
    @Test
    fun `should allow registering a LogReceiver`() {
        val mockReceiver = mockk<LogReceiver>()
        WasmBridge.setLogReceiver(mockReceiver)
    }
}
