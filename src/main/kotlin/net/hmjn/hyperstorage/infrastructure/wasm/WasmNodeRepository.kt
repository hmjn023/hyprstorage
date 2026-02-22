package net.hmjn.hyperstorage.infrastructure.wasm

import net.hmjn.hyperstorage.domain.repository.NodeRepository

/**
 * Implementation of NodeRepository using Wasm via ChicoryWasmClient.
 */
class WasmNodeRepository(private val client: ChicoryWasmClient) : NodeRepository {
    
    fun initNodeManager() {
        client.callFunction("init_node_manager")
    }

    override fun registerNode(
        x: Int,
        y: Int,
        z: Int,
        nodeType: Byte,
    ): Long {
        return client.callFunction(
            "register_node",
            x.toLong(),
            y.toLong(),
            z.toLong(),
            nodeType.toLong(),
        )
    }

    override fun unregisterNode(nodeId: Long) {
        client.callFunction("unregister_node", nodeId)
    }

    override fun setNodeActive(nodeId: Long, active: Byte) {
        client.callFunction("set_node_active", nodeId, active.toLong())
    }

    fun setNodeSleep(nodeId: Long, ticks: Long, backoffLvl: Byte) {
        client.callFunction("set_node_sleep", nodeId, ticks, backoffLvl.toLong())
    }

    fun pushSupply(sourceNodeId: Long, channelId: Long, itemId: Long, nbtHash: Long, quantity: Long) {
        client.callFunction("push_supply", sourceNodeId, channelId, itemId, nbtHash, quantity)
    }

    fun tickTransport() {
        client.callFunction("tick_transport")
    }

    fun getTransferBuffer(): List<TransferInstruction> {
        val size = client.callFunction("get_transfer_buffer_size").toInt()
        if (size == 0) return emptyList()

        val ptr = client.callFunction("get_transfer_buffer_ptr").toInt()
        val mem = client.getMemory() ?: throw IllegalStateException("Wasm memory missing")
        val bytes = mem.readBytes(ptr, size * 32)
        
        val instructions = mutableListOf<TransferInstruction>()
        for (i in 0 until size) {
            val offset = i * 32
            val sourceNodeId = readU32(bytes, offset)
            val targetNodeId = readU32(bytes, offset + 4)
            val itemId = readU32(bytes, offset + 8)
            // 4 bytes padding to align to 8
            val nbtHash = readU64(bytes, offset + 16)
            val quantity = readU64(bytes, offset + 24)
            
            instructions.add(TransferInstruction(sourceNodeId, targetNodeId, itemId, nbtHash, quantity))
        }

        client.callFunction("clear_transfer_buffer")
        return instructions
    }

    private fun readU32(bytes: ByteArray, offset: Int): Long {
        return ((bytes[offset].toLong() and 0xFF) or
                ((bytes[offset + 1].toLong() and 0xFF) shl 8) or
                ((bytes[offset + 2].toLong() and 0xFF) shl 16) or
                ((bytes[offset + 3].toLong() and 0xFF) shl 24))
    }

    private fun readU64(bytes: ByteArray, offset: Int): Long {
        return ((bytes[offset].toLong() and 0xFF) or
                ((bytes[offset + 1].toLong() and 0xFF) shl 8) or
                ((bytes[offset + 2].toLong() and 0xFF) shl 16) or
                ((bytes[offset + 3].toLong() and 0xFF) shl 24) or
                ((bytes[offset + 4].toLong() and 0xFF) shl 32) or
                ((bytes[offset + 5].toLong() and 0xFF) shl 40) or
                ((bytes[offset + 6].toLong() and 0xFF) shl 48) or
                ((bytes[offset + 7].toLong() and 0xFF) shl 56))
    }
}

data class TransferInstruction(
    val sourceNodeId: Long,
    val targetNodeId: Long,
    val itemId: Long,
    val nbtHash: Long,
    val quantity: Long,
)
