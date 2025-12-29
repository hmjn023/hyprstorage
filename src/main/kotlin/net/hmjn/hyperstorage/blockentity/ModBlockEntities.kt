package net.hmjn.hyperstorage.blockentity

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.block.ModBlocks
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ModBlockEntities {
    val REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Hyperstorage.ID)

    val HYPER_STORAGE_BLOCK_ENTITY =
            REGISTRY.register("hyper_storage_block_entity") { ->
                BlockEntityType.Builder.of(::HyperStorageBlockEntity, ModBlocks.HYPER_STORAGE_BLOCK)
                        .build(null)
            }
}
