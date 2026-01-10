package net.hmjn.hyperstorage.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class WasmIdMapperConcurrencyTest {
    @Test
    fun `should handle concurrent ID assignments safely`() {
        val mapper = WasmIdMapper()
        val threadCount = 10
        val iterationsPerThread = 1000
        val executor = Executors.newFixedThreadPool(threadCount)

        val itemNames = (1..100).map { "item_$it" }

        for (i in 1..threadCount) {
            executor.submit {
                for (j in 1..iterationsPerThread) {
                    val name = itemNames[j % itemNames.size]
                    mapper.getIdForName(name)
                    mapper.getNbtId(j.toLong())
                }
            }
        }

        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)

        // Verify state
        assertEquals(itemNames.size, mapper.getItemMap().size, "Should have mapped all unique items")
        
        // Ensure mapping is consistent
        val id1 = mapper.getIdForName("item_1")
        val id2 = mapper.getIdForName("item_1")
        assertEquals(id1, id2, "Mapping should be consistent even after concurrent access")
    }
}
