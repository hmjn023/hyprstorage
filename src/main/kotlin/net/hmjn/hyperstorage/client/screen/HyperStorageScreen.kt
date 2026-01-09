package net.hmjn.hyperstorage.client.screen

import net.hmjn.hyperstorage.menu.HyperStorageMenu
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory

class HyperStorageScreen(menu: HyperStorageMenu, playerInventory: Inventory, title: Component) :
    AbstractContainerScreen<HyperStorageMenu>(menu, playerInventory, title) {
    private val texture =
        ResourceLocation.fromNamespaceAndPath(
            "minecraft",
            "textures/gui/container/dispenser.png",
        )

    override fun render(
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        // Render the background
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        // Render tooltips
        renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun renderBg(
        guiGraphics: GuiGraphics,
        partialTick: Float,
        mouseX: Int,
        mouseY: Int,
    ) {
        val x = (width - imageWidth) / 2
        val y = (height - imageHeight) / 2
        guiGraphics.blit(texture, x, y, 0, 0, imageWidth, imageHeight)
    }
}
