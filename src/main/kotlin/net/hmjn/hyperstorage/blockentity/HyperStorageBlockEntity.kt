package net.hmjn.hyperstorage.blockentity

import net.hmjn.hyperstorage.Hyperstorage
import net.hmjn.hyperstorage.core.WasmBridge
import net.hmjn.hyperstorage.menu.HyperStorageMenu
import net.hmjn.hyperstorage.util.ItemHashUtil
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.Containers
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.items.ItemStackHandler

/**
 * BlockEntity for Hyper Storage Block Manages a simple inventory and communicates with Wasm for
 * state management
 */
class HyperStorageBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(ModBlockEntities.HYPER_STORAGE_BLOCK_ENTITY.get(), pos, state), MenuProvider {

    // NeoForge ItemHandler (9 slots for testing)
    val inventory =
            object : ItemStackHandler(9) {
                override fun onContentsChanged(slot: Int) {
                    super.onContentsChanged(slot)
                    this@HyperStorageBlockEntity.onInventoryChanged()
                }
            }

    // Track if inventory changed this tick
    private var inventoryChanged = false

    // Location ID for this block
    private val locationId: Int by lazy {
        ItemHashUtil.getLocationId(blockPos.x, blockPos.y, blockPos.z)
    }

    override fun saveAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.saveAdditional(tag, registries)
        tag.put("Inventory", inventory.serializeNBT(registries))
    }

    override fun loadAdditional(tag: CompoundTag, registries: HolderLookup.Provider) {
        super.loadAdditional(tag, registries)
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"))

        // Sync loaded inventory to Wasm
        syncFullInventoryToWasm()
    }

    /** Called when inventory changes */
    fun onInventoryChanged() {
        inventoryChanged = true
        setChanged()
    }

    /** Log current inventory state via Wasm */
    fun logInventoryState() {
        val itemCount = countItems()
        val wasmCount = WasmBridge.getLocationCount(locationId)
        val uniqueTypes = WasmBridge.getUniqueItemCount()

        Hyperstorage.LOGGER.info("Hyper Storage at $blockPos:")
        Hyperstorage.LOGGER.info("  Local inventory: $itemCount items")
        Hyperstorage.LOGGER.info("  Wasm inventory: $wasmCount items")
        Hyperstorage.LOGGER.info("  Unique item types (global): $uniqueTypes")

        // Test Wasm integration
        try {
            val wasmResult = WasmBridge.add(itemCount, 100)
            Hyperstorage.LOGGER.info("  Wasm calculation: $itemCount + 100 = $wasmResult")
        } catch (e: Exception) {
            Hyperstorage.LOGGER.error("Wasm integration error", e)
        }
    }

    /** Count total items in inventory */
    private fun countItems(): Int {
        var count = 0
        for (i in 0 until inventory.slots) {
            count += inventory.getStackInSlot(i).count
        }
        return count
    }

    // MenuProvider implementation
    override fun getDisplayName(): Component {
        return Component.translatable("container.hyperstorage.hyper_storage")
    }

    override fun createMenu(
            id: Int,
            playerInventory: Inventory,
            player: Player
    ): AbstractContainerMenu {
        return HyperStorageMenu(id, playerInventory, this)
    }

    /** Drop all items when block is broken */
    fun drops() {
        level?.let { level ->
            // Clear Wasm inventory for this location
            val removed = WasmBridge.clearLocation(locationId)
            Hyperstorage.LOGGER.debug(
                    "Cleared $removed item stacks from Wasm for location $locationId"
            )

            // Drop physical items
            val simpleContainer = SimpleContainer(inventory.slots)
            for (i in 0 until inventory.slots) {
                simpleContainer.setItem(i, inventory.getStackInSlot(i))
            }
            Containers.dropContents(level, blockPos, simpleContainer)
        }
    }

    companion object {
        /** Server-side tick method */
        fun serverTick(
                level: Level,
                pos: BlockPos,
                state: BlockState,
                blockEntity: HyperStorageBlockEntity
        ) {
            // If inventory changed, sync with Wasm
            if (blockEntity.inventoryChanged) {
                blockEntity.syncInventoryToWasm()
                blockEntity.inventoryChanged = false
            }
        }
    }

    /** Sync full inventory state to Wasm (on load) */
    private fun syncFullInventoryToWasm() {
        // Clear existing data for this location
        WasmBridge.clearLocation(locationId)

        // Add all items and update snapshot
        for (i in 0 until inventory.slots) {
            val stack = inventory.getStackInSlot(i)

            // Update snapshot
            previousInventory[i] = stack.copy()

            if (!stack.isEmpty) {
                val itemId = ItemHashUtil.getItemId(stack)
                val nbtHash = ItemHashUtil.getNbtHash(stack)
                val quantity = stack.count.toLong()

                WasmBridge.addItem(itemId, nbtHash, quantity, locationId)
            }
        }

        Hyperstorage.LOGGER.debug("Synced full inventory to Wasm for location $locationId")
    }

    // Snapshot of the previous tick's inventory state for diffing
    private val previousInventory =
            net.minecraft.core.NonNullList.withSize(9, net.minecraft.world.item.ItemStack.EMPTY)

    /**
     * Sync inventory changes to Wasm (incremental) Compares current inventory with previous
     * snapshot and sends only changes.
     */
    private fun syncInventoryToWasm() {
        var changes = 0

        for (i in 0 until inventory.slots) {
            val currentStack = inventory.getStackInSlot(i)
            val previousStack = previousInventory[i]

            // Check if stack changed
            if (!net.minecraft.world.item.ItemStack.matches(currentStack, previousStack)) {
                // 1. Remove previous item if it existed
                if (!previousStack.isEmpty) {
                    val prevId = ItemHashUtil.getItemId(previousStack)
                    val prevNbt = ItemHashUtil.getNbtHash(previousStack)
                    val prevCount = previousStack.count.toLong()
                    WasmBridge.removeItem(prevId, prevNbt, prevCount, locationId)
                }

                // 2. Add new item if it exists
                if (!currentStack.isEmpty) {
                    val newId = ItemHashUtil.getItemId(currentStack)
                    val newNbt = ItemHashUtil.getNbtHash(currentStack)
                    val newCount = currentStack.count.toLong()
                    WasmBridge.addItem(newId, newNbt, newCount, locationId)
                }

                // 3. Update snapshot
                previousInventory[i] = currentStack.copy()
                changes++
            }
        }

        if (changes > 0) {
            Hyperstorage.LOGGER.debug("Synced $changes slot changes to Wasm at $blockPos")
        }
    }
}
