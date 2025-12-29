package net.hmjn.hyperstorage.item

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.block.ModBlocks
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ModItems {
    val REGISTRY = DeferredRegister.createItems(Hyperstorage.ID)

    // Block items
    val EXAMPLE_BLOCK_ITEM by REGISTRY.register("example_block") { ->
        BlockItem(ModBlocks.EXAMPLE_BLOCK, Item.Properties())
    }

    val HYPER_STORAGE_BLOCK_ITEM by REGISTRY.register("hyper_storage_block") { ->
        BlockItem(ModBlocks.HYPER_STORAGE_BLOCK, Item.Properties())
    }
}
