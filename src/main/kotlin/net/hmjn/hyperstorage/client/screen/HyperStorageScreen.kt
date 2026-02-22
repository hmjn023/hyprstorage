package net.hmjn.hyperstorage.client.screen

import net.hmjn.hyperstorage.menu.HyperStorageMenu
import net.hmjn.hyperstorage.network.payload.FetchNodeNamesPayload
import net.hmjn.hyperstorage.network.payload.SetCustomNamePayload
import net.hmjn.hyperstorage.network.payload.SetTargetNamePayload
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Inventory
import net.neoforged.neoforge.network.PacketDistributor

class HyperStorageScreen(menu: HyperStorageMenu, playerInventory: Inventory, title: Component) :
    AbstractContainerScreen<HyperStorageMenu>(menu, playerInventory, title) {
    private val texture = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png")

    init {
        // Double chest dimensions
        imageWidth = 176
        imageHeight = 222
        inventoryLabelY = imageHeight - 94
    }

    private lateinit var nameBox: EditBox
    private var availableNames: List<String> = emptyList()
    private val targetButtons = mutableListOf<Button>()
    private var currentPage: Int = 0

    override fun init() {
        super.init()
        val x = (width - imageWidth) / 2
        val y = (height - imageHeight) / 2

        // Edit box for custom name
        nameBox = EditBox(font, x + 8, y + 20, 80, 16, Component.literal("Node Name"))
        nameBox.value = menu.blockEntity.customName
        addRenderableWidget(nameBox)

        // Confirm button for custom name
        val confirmBtn =
            Button.builder(Component.literal("V")) {
                val newName = nameBox.value
                menu.blockEntity.customName = newName
                PacketDistributor.sendToServer(SetCustomNamePayload(menu.blockEntity.blockPos, newName))
                rebuildTargetList()
            }.bounds(x + 92, y + 20, 16, 16).build()
        addRenderableWidget(confirmBtn)

        rebuildTargetList()

        // Fetch the list of available names from the server
        PacketDistributor.sendToServer(FetchNodeNamesPayload())
    }

    fun updateAvailableNames(names: List<String>) {
        this.availableNames = names
        rebuildTargetList()
    }

    private fun rebuildTargetList() {
        val x = (width - imageWidth) / 2
        val y = (height - imageHeight) / 2

        // Remove old target buttons
        targetButtons.forEach { removeWidget(it) }
        targetButtons.clear()

        val pageItemsCount = 6
        val filteredNames = availableNames.filter { it != menu.blockEntity.customName && it.isNotEmpty() }.sorted()
        val maxPage = maxOf(0, (filteredNames.size - 1) / pageItemsCount)
        currentPage = currentPage.coerceIn(0, maxPage)

        val startIndex = currentPage * pageItemsCount
        val endIndex = minOf(startIndex + pageItemsCount, filteredNames.size)
        val visibleNames = filteredNames.subList(startIndex, endIndex)

        var offsetY = 38
        for (name in visibleNames) {
            val isSelected = name == menu.blockEntity.targetName
            val selectBtn =
                Button.builder(Component.literal(if (isSelected) "[X] $name" else "[ ] $name")) {
                    val finalTarget = if (isSelected) "" else name
                    PacketDistributor.sendToServer(SetTargetNamePayload(menu.blockEntity.blockPos, finalTarget))
                    // Optimistically update local state for render
                    menu.blockEntity.targetName = finalTarget
                    rebuildTargetList()
                }.bounds(x + 112, y + offsetY, 50, 14).build()

            addRenderableWidget(selectBtn)
            targetButtons.add(selectBtn)
            offsetY += 16
        }

        // Vertical Pagination buttons (Up / Down)
        val upBtn =
            Button.builder(Component.literal("^")) {
                currentPage = maxOf(0, currentPage - 1)
                rebuildTargetList()
            }.bounds(x + 164, y + 38, 12, 45).build()

        val downBtn =
            Button.builder(Component.literal("v")) {
                currentPage = minOf(maxPage, currentPage + 1)
                rebuildTargetList()
            }.bounds(x + 164, y + 85, 12, 45).build()

        upBtn.active = currentPage > 0
        downBtn.active = currentPage < maxPage

        addRenderableWidget(upBtn)
        targetButtons.add(upBtn)
        addRenderableWidget(downBtn)
        targetButtons.add(downBtn)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
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
        // Use the large row height by rendering it twice if needed or just letting it stretch
        // A generic_54 has 6 rows of inventory (222 pixels tall).
        guiGraphics.blit(texture, x, y, 0, 0, imageWidth, imageHeight)

        guiGraphics.drawString(font, "Target Node Selection", x + 8, y + 6, 4210752, false)
    }
}
