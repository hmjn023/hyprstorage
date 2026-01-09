package net.hmjn.hyperstorage.util

import net.hmjn.hyperstorage.core.WasmIdManager
import net.minecraft.world.item.ItemStack
import java.security.MessageDigest

/** Utility for hashing items and NBT data for Wasm integration */
object ItemHashUtil {
    fun getItemId(stack: ItemStack): Int {
        // Use the centralized ID manager for safe, collision-free IDs
        return WasmIdManager.getId(stack)
    }

    /** Calculate a deterministic hash for item data Returns 0 if no custom data */
    fun getNbtHash(stack: ItemStack): Long {
        // In Minecraft 1.21.1, use components instead of NBT
        val components = stack.components
        if (components.isEmpty) return 0L

        // Use the components string representation for hashing
        val componentsString = components.toString()

        // Use MD5 for deterministic hashing
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(componentsString.toByteArray())

        // Convert first 8 bytes to long
        var hash = 0L
        for (i in 0 until 8.coerceAtMost(hashBytes.size)) {
            hash = (hash shl 8) or (hashBytes[i].toLong() and 0xFF)
        }

        return hash
    }

    /** Get location ID from BlockPos Simple hash of coordinates */
    fun getLocationId(
        x: Int,
        y: Int,
        z: Int,
    ): Int {
        // Simple but effective hash
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }
}
