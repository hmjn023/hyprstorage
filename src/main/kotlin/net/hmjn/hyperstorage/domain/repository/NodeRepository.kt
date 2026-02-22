package net.hmjn.hyperstorage.domain.repository

/**
 * Repository interface for managing logical nodes in the Wasm backend.
 */
interface NodeRepository {
    /**
     * Registers a new node at the specified coordinates.
     * @param x X coordinate (block position)
     * @param y Y coordinate (block position)
     * @param z Z coordinate (block position)
     * @param nodeType 0 for Importer, 1 for Exporter
     * @return The generated unique Node ID
     */
    fun registerNode(
        x: Int,
        y: Int,
        z: Int,
        nodeType: Byte,
    ): Long

    /**
     * Unregisters and removes a node by its ID.
     * @param nodeId The unique ID of the node
     */
    fun unregisterNode(nodeId: Long)

    /**
     * Sets the active state of a node (e.g., due to redstone control).
     * @param nodeId The unique ID of the node
     * @param active 1 for Active, 0 for Inactive
     */
    fun setNodeActive(nodeId: Long, active: Byte)
}
