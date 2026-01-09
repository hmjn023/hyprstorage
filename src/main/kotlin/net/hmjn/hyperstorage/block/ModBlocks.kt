package net.hmjn.hyperstorage.block

import net.hmjn.hyperstorage.Hyperstorage
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue // THIS LINE IS REQUIRED FOR USING PROPERTY DELEGATES

object ModBlocks {
    val REGISTRY = DeferredRegister.createBlocks(Hyperstorage.ID)

    // Example block (keep for reference)
    val EXAMPLE_BLOCK by REGISTRY.register("example_block") { ->
        Block(BlockBehaviour.Properties.of().lightLevel { 15 }.strength(3.0f))
    }

    // Hyper Storage Block - Main storage block with Wasm integration
    val HYPER_STORAGE_BLOCK by REGISTRY.register("hyper_storage_block") { ->
        HyperStorageBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0f, 6.0f)
                .requiresCorrectToolForDrops(),
        )
    }
}
