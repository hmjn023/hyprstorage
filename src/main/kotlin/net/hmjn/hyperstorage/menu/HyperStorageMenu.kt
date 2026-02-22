package net.hmjn.hyperstorage.menu

import net.hmjn.hyperstorage.blockentity.HyperStorageBlockEntity
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.items.SlotItemHandler

/** Menu for Hyper Storage Block Simple 9-slot inventory GUI */
class HyperStorageMenu(
    id: Int,
    playerInventory: Inventory,
    val blockEntity: HyperStorageBlockEntity,
) : AbstractContainerMenu(ModMenuTypes.HYPER_STORAGE_MENU.get(), id) {
    // Constructor for client-side (from packet)
    constructor(
        id: Int,
        playerInventory: Inventory,
        extraData: FriendlyByteBuf,
    ) : this(
        id,
        playerInventory,
        playerInventory.player.level().getBlockEntity(extraData.readBlockPos()) as
            HyperStorageBlockEntity,
    )

    init {
        // Add storage slots (3x3 grid) using SlotItemHandler for capabilities
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                // Determine if this is a slot we want to expose (0-8)
                val index = col + row * 9
                if (index < 9) {
                    // Correct internal index mapping: 3x3 grid
                    // Actually logic in previous code was: col (0-2) + row (0-2) * 3
                }
            }
        }

        // 3x3 grid moved left, leaving tons of space on the right for names
        for (row in 0 until 3) {
            for (col in 0 until 3) {
                val index = col + row * 3
                addSlot(SlotItemHandler(blockEntity.inventory, index, 8 + col * 18, 54 + row * 18))
            }
        }

        // Add player inventory (standard Slots for 222 height container)
        for (row in 0 until 3) {
            for (col in 0 until 9) {
                addSlot(Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18))
            }
        }

        // Add player hotbar
        for (col in 0 until 9) {
            addSlot(Slot(playerInventory, col, 8 + col * 18, 198))
        }
    }

    override fun quickMoveStack(
        player: Player,
        index: Int,
    ): ItemStack {
        var itemstack = ItemStack.EMPTY
        val slot = slots[index]

        if (slot.hasItem()) {
            val stackInSlot = slot.item
            itemstack = stackInSlot.copy()

            if (index < 9) {
                // Moving from storage to player inventory
                if (!moveItemStackTo(stackInSlot, 9, slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else {
                // Moving from player inventory to storage
                if (!moveItemStackTo(stackInSlot, 0, 9, false)) {
                    return ItemStack.EMPTY
                }
            }

            if (stackInSlot.isEmpty) {
                slot.setByPlayer(ItemStack.EMPTY)
            } else {
                slot.setChanged()
            }
        }

        return itemstack
    }

    override fun stillValid(player: Player): Boolean {
        // Just check distance
        return player.distanceToSqr(
            blockEntity.blockPos.x + 0.5,
            blockEntity.blockPos.y + 0.5,
            blockEntity.blockPos.z + 0.5,
        ) <= 64.0
    }
}
