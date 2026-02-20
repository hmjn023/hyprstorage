package net.hmjn.hyperstorage.client

import net.hmjn.hyperstorage.client.screen.HyperStorageScreen
import net.hmjn.hyperstorage.menu.ModMenuTypes
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

object ClientSetup {
    // Called manually from Hyperstorage.onClientSetup via MOD_BUS.addListener
    internal fun registerScreens(event: RegisterMenuScreensEvent) {
        // Register the screen for the menu type
        event.register(ModMenuTypes.HYPER_STORAGE_MENU.get(), ::HyperStorageScreen)
    }
}
