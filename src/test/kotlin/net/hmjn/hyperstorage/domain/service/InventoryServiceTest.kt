package net.hmjn.hyperstorage.domain.service

import io.mockk.mockk
import io.mockk.verify
import net.hmjn.hyperstorage.domain.model.ItemInfo
import net.hmjn.hyperstorage.domain.repository.InventoryRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InventoryServiceTest {
    private lateinit var repository: InventoryRepository
    private lateinit var service: InventoryService

    @BeforeEach
    fun setup() {
        repository = mockk(relaxed = true)
        service = InventoryService(repository)
    }

    @Test
    fun testSyncInventory_ItemAdded() {
        val locId = 1
        val current = listOf(ItemInfo(1, 100L, 10L))
        val previous = listOf(ItemInfo.EMPTY)

        service.syncInventory(locId, current, previous)

        verify { repository.addItem(1, 100L, 10L, locId) }
    }

    @Test
    fun testSyncInventory_ItemRemoved() {
        val locId = 1
        val current = listOf(ItemInfo.EMPTY)
        val previous = listOf(ItemInfo(1, 100L, 10L))

        service.syncInventory(locId, current, previous)

        verify { repository.removeItem(1, 100L, 10L, locId) }
    }

    @Test
    fun testSyncInventory_ItemChanged() {
        val locId = 1
        val current = listOf(ItemInfo(2, 200L, 5L))
        val previous = listOf(ItemInfo(1, 100L, 10L))

        service.syncInventory(locId, current, previous)

        verify { repository.removeItem(1, 100L, 10L, locId) }
        verify { repository.addItem(2, 200L, 5L, locId) }
    }
}
