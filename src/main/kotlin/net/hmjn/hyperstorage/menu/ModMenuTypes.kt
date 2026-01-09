package net.hmjn.hyperstorage.menu

import net.hmjn.hyperstorage.Hyperstorage
import net.minecraft.core.registries.Registries
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredRegister

object ModMenuTypes {
    val REGISTRY = DeferredRegister.create(Registries.MENU, Hyperstorage.ID)

    val HYPER_STORAGE_MENU =
        REGISTRY.register("hyper_storage_menu") { ->
            IMenuTypeExtension.create(::HyperStorageMenu)
        }
}
