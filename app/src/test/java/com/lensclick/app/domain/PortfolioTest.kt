package com.lensclick.app.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioTest {
    @Test
    fun limitsMatchDocumentedContract() {
        assertEquals(12, PortfolioLimits.MAX_ITEMS)
        assertEquals(4_000_000, PortfolioLimits.MAX_IMAGE_BYTES)
        assertEquals(1, PortfolioLimits.MAX_COVERS)
    }

    @Test
    fun itemCarriesMetadataAndCoverFlag() {
        val item = PortfolioItem(
            id = 7,
            caption = "Praia",
            altText = "Retrato na praia",
            position = 1,
            isCover = true,
            category = "portrait",
            imageUrl = "/api/photographers/ana-12/portfolio/7"
        )
        assertEquals(7L, item.id)
        assertEquals("Retrato na praia", item.altText)
        assertTrue(item.isCover)
        assertEquals("portrait", item.category)
    }

    @Test
    fun gatewayAddListAndRemoveItems() = runTest {
        val gateway = FakePortfolioGateway()
        assertTrue(gateway.list().isEmpty())

        val first = gateway.add(ByteArray(1), altText = "Alt", caption = "Cap", category = "portrait")
        val second = gateway.add(ByteArray(1), altText = "Alt2", caption = "Cap2", category = "wedding")
        assertEquals(listOf(first, second), gateway.list())

        gateway.remove(first.id)
        assertEquals(listOf(second), gateway.list())
    }

    @Test
    fun gatewayUpdateReplacesItemsAndOrder() = runTest {
        val gateway = FakePortfolioGateway()
        val first = gateway.add(ByteArray(1), "Alt", "Cap", "portrait")
        val reordered = listOf(first.copy(position = 0, isCover = true))

        assertEquals(reordered, gateway.update(reordered))
        assertEquals(reordered, gateway.list())
        assertTrue(gateway.list().single().isCover)
    }
}

private class FakePortfolioGateway : PortfolioGateway {
    private val items = mutableListOf<PortfolioItem>()
    private var nextId = 1L

    override suspend fun list(): List<PortfolioItem> = items.toList()

    override suspend fun add(image: ByteArray, altText: String, caption: String, category: String): PortfolioItem {
        val id = nextId++
        val item = PortfolioItem(id, caption, altText, items.size, false, category, "memory://$id")
        items += item
        return item
    }

    override suspend fun update(items: List<PortfolioItem>): List<PortfolioItem> {
        this.items.clear()
        this.items += items
        return this.items.toList()
    }

    override suspend fun remove(id: Long) {
        items.removeAll { it.id == id }
    }
}