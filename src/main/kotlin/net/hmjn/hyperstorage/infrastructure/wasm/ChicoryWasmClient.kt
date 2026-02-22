package net.hmjn.hyperstorage.infrastructure.wasm

import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.ImportValues
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.Parser
import java.io.InputStream

/**
 * Low-level client for interacting with the Wasm module using Chicory.
 */
class ChicoryWasmClient {
    private var instance: Instance? = null
    private val hostFunctions = mutableListOf<HostFunction>()

    /**
     * Adds a host function to be registered when the module is loaded.
     */
    fun addHostFunction(hostFunction: HostFunction) {
        hostFunctions.add(hostFunction)
    }

    /**
     * Loads the Wasm module from the given stream.
     */
    fun load(inputStream: InputStream) {
        val module = Parser.parse(inputStream)
        val imports = ImportValues.builder()
        hostFunctions.forEach { imports.addFunction(it) }

        instance =
            Instance.builder(module)
                .withImportValues(imports.build())
                .build()
    }

    /**
     * Returns the memory of the current instance.
     */
    fun getMemory() = instance?.memory()

    /**
     * Calls a Wasm function with the given name and arguments.
     * Returns the result as a Long.
     */
    fun callFunction(
        name: String,
        vararg args: Long,
    ): Long {
        val inst = instance ?: throw IllegalStateException("Wasm instance not loaded")
        val export = inst.export(name) ?: throw IllegalArgumentException("Wasm function '$name' not found")
        val results = export.apply(*args)
        return if (results != null && results.isNotEmpty()) results[0] else 0L
    }

    /**
     * Extracts the full inventory snapshot from the active Wasm module.
     */
    fun getSnapshotBytes(): ByteArray {
        val size = callFunction("get_inventory_snapshot_size").toInt()
        if (size == 0) return ByteArray(0)

        val ptr = callFunction("alloc", size.toLong()).toInt()
        callFunction("copy_inventory_to_buffer", ptr.toLong())

        val mem = instance?.memory() ?: throw IllegalStateException("Wasm memory missing")
        val bytes = mem.readBytes(ptr, size)

        callFunction("dealloc", ptr.toLong(), size.toLong())
        return bytes
    }

    /**
     * Restores the full inventory snapshot into the active Wasm module.
     */
    fun restoreSnapshotBytes(bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) return false
        val size = bytes.size
        val ptr = callFunction("alloc", size.toLong()).toInt()

        val mem = instance?.memory() ?: throw IllegalStateException("Wasm memory missing")
        mem.write(ptr, bytes)

        val result = callFunction("reconstruct_from_binary_dump", ptr.toLong(), size.toLong())
        
        callFunction("dealloc", ptr.toLong(), size.toLong())
        return result == 1L
    }
}
