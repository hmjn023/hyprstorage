package net.hmjn.hyperstorage.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class WasmIdMapperTest {
    @Test
    fun `should assign new IDs on demand`() {
        val mapper = WasmIdMapper()
        val ironIngot = "minecraft:iron_ingot"
        val goldIngot = "minecraft:gold_ingot"

        val id1 = mapper.getIdForName(ironIngot)
        val id2 = mapper.getIdForName(goldIngot)

        assertNotEquals(0, id1, "ID should not be 0")
        assertNotEquals(0, id2, "ID should not be 0")
        assertNotEquals(id1, id2, "Different items should have different IDs")
    }

    @Test
    fun `should return same ID for same item`() {
        val mapper = WasmIdMapper()
        val ironIngot = "minecraft:iron_ingot"

        val id1 = mapper.getIdForName(ironIngot)
        val id2 = mapper.getIdForName(ironIngot)

        assertEquals(id1, id2, "Same item should have same ID")
    }

    @Test
    fun `should reverse map ID to name`() {
        val mapper = WasmIdMapper()
        val ironIngotName = "minecraft:iron_ingot"

        val id = mapper.getIdForName(ironIngotName)
        val name = mapper.getNameForId(id)

        assertEquals(ironIngotName, name, "Should be able to get name from ID")
    }

    @Test
    fun `should assign NBT IDs based on hashes`() {
        val mapper = WasmIdMapper()
        val hash1 = 12345L
        val hash2 = 67890L

        val id1 = mapper.getNbtId(hash1)
        val id2 = mapper.getNbtId(hash2)

        assertNotEquals(0, id1, "NBT ID should not be 0 for non-zero hash")
        assertNotEquals(0, id2, "NBT ID should not be 0 for non-zero hash")
        assertNotEquals(id1, id2, "Different hashes should have different NBT IDs")
    }

    @Test
    fun `should return same NBT ID for same hash`() {
        val mapper = WasmIdMapper()
        val hash = 12345L

        val id1 = mapper.getNbtId(hash)
        val id2 = mapper.getNbtId(hash)

        assertEquals(id1, id2, "Same hash should have same NBT ID")
    }

    @Test
    fun `should return 0 for null hash`() {
        val mapper = WasmIdMapper()
        assertEquals(0, mapper.getNbtId(0L), "Null hash should map to ID 0")
    }

    @Test
    fun `should preserve data after save and load cycle`() {
        val original = WasmIdMapper()
        original.getIdForName("minecraft:iron_ingot")
        original.getIdForName("minecraft:gold_ingot")
        original.getNbtId(12345L)

        val items = original.getItemMap()
        val nbts = original.getNbtMap()

        val restored = WasmIdMapper()
        restored.loadData(items, nbts)

        assertEquals(original.getIdForName("minecraft:iron_ingot"), restored.getIdForName("minecraft:iron_ingot"))
        assertEquals(original.getNameForId(2), restored.getNameForId(2))
        assertEquals(original.getNbtId(12345L), restored.getNbtId(12345L))

        // Ensure next ID is correct
        val newId = restored.getIdForName("minecraft:diamond")
        assertEquals(3, newId, "Next item ID should be correctly initialized")
    }
}
