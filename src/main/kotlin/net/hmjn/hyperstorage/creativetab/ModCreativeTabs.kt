package net.hmjn.hyperstorage.creativetab

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.block.ModBlocks
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object ModCreativeTabs {
    val REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Hyperstorage.ID)

    val HYPER_STORAGE_TAB by REGISTRY.register("hyper_storage_tab") { ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.hyperstorage"))
            .icon { ModBlocks.HYPER_STORAGE_BLOCK.asItem().defaultInstance }
            .displayItems { _, output ->
                // Add all mod blocks/items here
                output.accept(ModBlocks.EXAMPLE_BLOCK)
                output.accept(ModBlocks.HYPER_STORAGE_BLOCK)
            }
            .build()
    }
}
