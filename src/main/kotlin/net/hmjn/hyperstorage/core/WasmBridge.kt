package net.hmjn.hyperstorage.core

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.wasm.types.ValueType
import net.hmjn.hyperstorage.infrastructure.logging.LogReceiver

/**
 * Singleton bridge between Kotlin and Wasm.
 * Manages FFI callbacks and shared state.
 */
object WasmBridge {
    private var logReceiver: LogReceiver? = null

    /**
     * Sets the receiver for Wasm logs.
     */
    fun setLogReceiver(receiver: LogReceiver) {
        this.logReceiver = receiver
    }

    /**
     * Creates a HostFunction that Rust can call to log messages.
     * Expects: (level: i32, file_ptr: i32, file_len: i32, line: i32, msg_ptr: i32, msg_len: i32)
     */
    fun createLogHostFunction(): HostFunction {
        return HostFunction(
            "env",
            "wasm_log",
            listOf(
                // level
                ValueType.I32,
                // file_ptr
                ValueType.I32,
                // file_len
                ValueType.I32,
                // line
                ValueType.I32,
                // msg_ptr
                ValueType.I32,
                // msg_len
                ValueType.I32,
            ),
            listOf(),
        ) { instance, args ->
            val receiver = logReceiver ?: return@HostFunction null

            val level = args[0].toInt()
            val filePtr = args[1].toInt()
            val fileLen = args[2].toInt()
            val line = args[3].toInt()
            val msgPtr = args[4].toInt()
            val msgLen = args[5].toInt()

            val memory = instance.memory()
            val file = String(memory.readBytes(filePtr, fileLen))
            val msg = String(memory.readBytes(msgPtr, msgLen))

            receiver.log(level, file, line, msg)
            null
        }
    }
}
