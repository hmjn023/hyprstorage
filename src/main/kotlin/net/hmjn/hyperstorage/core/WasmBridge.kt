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
        } catch (e: Exception) {
            LOGGER.error("Failed to load Wasm module", e)
        }
    }

    fun add(a: Int, b: Int): Int {
        val inst = instance ?: return -1
        val addFn = inst.export("add")
        val result = addFn.apply(a.toLong(), b.toLong())
        return result[0].toInt()
    }
}
