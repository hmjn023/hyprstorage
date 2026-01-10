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
}