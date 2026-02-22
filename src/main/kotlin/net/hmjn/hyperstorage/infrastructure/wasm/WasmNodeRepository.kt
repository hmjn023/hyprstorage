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
}
