package net.hmjn.hyperstorage.infrastructure.wasm

import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import java.io.InputStream

/**
 * Low-level client for interacting with the Wasm module using Chicory.
 */
class ChicoryWasmClient {
    private var instance: Instance? = null

    /**
     * Loads the Wasm module from the given stream.
     */
    fun load(inputStream: InputStream) {
        val module = Parser.parse(inputStream)
        instance = Instance.builder(module).build()
    }

    /**
     * Calls a Wasm function with the given name and arguments.
     * Returns the result as a Long.
     */
    fun callFunction(name: String, vararg args: Long): Long {
        val inst = instance ?: throw IllegalStateException("Wasm instance not loaded")
        val export = inst.export(name) ?: throw IllegalArgumentException("Wasm function '$name' not found")
        val results = export.apply(*args)
        return if (results != null && results.isNotEmpty()) results[0] else 0L
    }
}

