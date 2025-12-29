package net.hmjn.hyperstorage.client

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.client.screen.HyperStorageScreen
import net.hmjn.hyperstorage.menu.ModMenuTypes
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

@EventBusSubscriber(
        modid = Hyperstorage.ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = [Dist.CLIENT]
)
object ClientSetup {

    @SubscribeEvent
    fun registerScreens(event: RegisterMenuScreensEvent) {
        // Register the screen for the menu type
        event.register(ModMenuTypes.HYPER_STORAGE_MENU.get(), ::HyperStorageScreen)
    }
}
