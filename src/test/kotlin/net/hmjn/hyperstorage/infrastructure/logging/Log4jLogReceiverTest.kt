package net.hmjn.hyperstorage.infrastructure.logging

import io.mockk.mockk
import io.mockk.verify
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import org.junit.jupiter.api.Test

class Log4jLogReceiverTest {
    @Test
    fun `should log with correct levels and format`() {
        val mockLogger = mockk<Logger>(relaxed = true)
        val receiver = Log4jLogReceiver(mockLogger)

        receiver.log(1, "test.rs", 10, "Error message")
        verify { mockLogger.log(Level.ERROR, "[Wasm] test.rs:10 - Error message") }

        receiver.log(2, "test.rs", 20, "Warn message")
        verify { mockLogger.log(Level.WARN, "[Wasm] test.rs:20 - Warn message") }

        receiver.log(3, "test.rs", 30, "Info message")
        verify { mockLogger.log(Level.INFO, "[Wasm] test.rs:30 - Info message") }

        receiver.log(4, "test.rs", 40, "Debug message")
        verify { mockLogger.log(Level.DEBUG, "[Wasm] test.rs:40 - Debug message") }

        receiver.log(5, "test.rs", 50, "Trace message")
        verify { mockLogger.log(Level.TRACE, "[Wasm] test.rs:50 - Trace message") }
    }
}
