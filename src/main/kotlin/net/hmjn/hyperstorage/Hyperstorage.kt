package net.hmjn.hyperstorage

import net.hmjn.hyperstorage.block.ModBlocks
import net.hmjn.hyperstorage.client.ClientSetup
import net.hmjn.hyperstorage.core.WasmIdManager
import net.minecraft.client.Minecraft
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.neoforged.neoforge.common.NeoForge
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist

/**
 * Main mod class.
 *
 * An example for blocks is in the `blocks` package of this mod.
 */
@Mod(Hyperstorage.ID)
object Hyperstorage {
    const val ID = "hyperstorage"

    // the logger for our mod
    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.log(Level.INFO, "Hello world!")

        // Register the KDeferredRegister to the mod-specific event bus
        ModBlocks.REGISTRY.register(MOD_BUS)
        net.hmjn.hyperstorage.blockentity.ModBlockEntities.REGISTRY.register(MOD_BUS)
        net.hmjn.hyperstorage.item.ModItems.REGISTRY.register(MOD_BUS)
        net.hmjn.hyperstorage.creativetab.ModCreativeTabs.REGISTRY.register(MOD_BUS)
        net.hmjn.hyperstorage.menu.ModMenuTypes.REGISTRY.register(MOD_BUS)

        // Register lifecycle events manually (avoids AutoKotlinEventBusSubscriber / Bindings bug)
        MOD_BUS.addListener(::onCommonSetup)
        MOD_BUS.addListener(::registerCapabilities)
        MOD_BUS.addListener(net.hmjn.hyperstorage.network.ModNetworking::register)

        // Register game-level events (FORGE bus) for WasmIdManager
        NeoForge.EVENT_BUS.addListener(WasmIdManager::onLevelSave)
        NeoForge.EVENT_BUS.addListener(WasmIdManager::onLevelLoad)

        val obj =
            runForDist(
                clientTarget = {
                    MOD_BUS.addListener(::onClientSetup)
                    // Register screen directly here so it fires before RegisterMenuScreensEvent
                    MOD_BUS.addListener(ClientSetup::registerScreens)
                    Minecraft.getInstance()
                },
                serverTarget = {
                    MOD_BUS.addListener(::onServerSetup)
                    "test"
                },
            )

        println(obj)
    }

    /**
     * This is used for initializing client specific things such as renderers and keymaps Fired on
     * the mod specific event bus.
     */
    private fun onClientSetup(event: FMLClientSetupEvent) {
        LOGGER.log(Level.INFO, "Initializing client...")
    }

    /** Fired on the global Forge bus. */
    private fun onServerSetup(event: FMLDedicatedServerSetupEvent) {
        LOGGER.log(Level.INFO, "Server starting...")
    }

    private fun onCommonSetup(event: FMLCommonSetupEvent) {
        LOGGER.log(Level.INFO, "Hello! This is working!")

        // Load Wasm via InventoryManager
        try {
            val wasmStream = javaClass.getResourceAsStream("/wasm/hyper_visor_storage_wasm.wasm")
            if (wasmStream != null) {
                net.hmjn.hyperstorage.infrastructure.InventoryManager.loadWasm(wasmStream)

                // Test call using repository (optional test)
                val repo = net.hmjn.hyperstorage.infrastructure.InventoryManager.getInventoryRepository()
                val result = repo.addItem(1, 0L, 10L, 0)
                LOGGER.info("Wasm Repository Test: Added 10 items, result = $result")
            } else {
                LOGGER.error("Wasm file not found in resources!")
            }
        } catch (e: Exception) {
            LOGGER.error("Error testing Wasm via InventoryManager", e)
        }
    }

    private fun registerCapabilities(event: net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent) {
        event.registerBlockEntity(
            net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
            net.hmjn.hyperstorage.blockentity.ModBlockEntities.HYPER_STORAGE_BLOCK_ENTITY.get(),
        ) { blockEntity: net.hmjn.hyperstorage.blockentity.HyperStorageBlockEntity, _ ->
            blockEntity.inventory
        }
    }
}
