package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.domain.PortfolioItem
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioGatewayTest {
    private val itemJson =
        """{"id":7,"caption":"Praia","altText":"Retrato","position":1,"isCover":true,"category":"portrait","imageUrl":"/api/account/portfolio/7/image?v=1"}"""

    @Test
    fun parseListReadsItems() {
        val items = parsePortfolioList("""{"portfolio":[$itemJson]}""")
        assertEquals(1, items.size)
        assertEquals(7L, items[0].id)
        assertEquals("Retrato", items[0].altText)
        assertTrue(items[0].isCover)
        assertEquals("/api/account/portfolio/7/image?v=1", items[0].imageUrl)
    }

    @Test
    fun listSendsBearerGet() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"portfolio":[$itemJson]}"""))
        val store = InMemorySessionStore().apply { writeAccessToken("token") }

        val items = HttpPortfolioGateway(transport, store).list()

        assertEquals(1, items.size)
        assertEquals("GET", transport.lastRequest?.method)
        assertEquals("/api/account/portfolio", transport.lastRequest?.path)
        assertEquals("Bearer token", transport.lastRequest?.headers?.get("Authorization"))
    }

    @Test
    fun addSendsMultipartWithBearerAndParsesItem() = runTest {
        val transport = FakeHttpTransport(HttpResponse(201, body = """{"item":$itemJson}"""))
        val store = InMemorySessionStore().apply { writeAccessToken("token") }

        val item = HttpPortfolioGateway(transport, store).add(
            byteArrayOf(1, 2, 3), "image/jpeg", altText = "Retrato", caption = "Praia", category = "portrait"
        )

        assertEquals(7L, item.id)
        assertEquals("POST", transport.lastRequest?.method)
        assertEquals("/api/account/portfolio", transport.lastRequest?.path)
        val contentType = transport.lastRequest?.headers?.get("Content-Type")!!
        assertTrue(contentType.startsWith("multipart/form-data; boundary="))
        assertEquals("Bearer token", transport.lastRequest?.headers?.get("Authorization"))
        val body = String(transport.lastRequest!!.bodyBytes!!, Charsets.UTF_8)
        assertTrue(body.contains("name=\"image\""))
        assertTrue(body.contains("Content-Type: image/jpeg"))
        assertTrue(body.contains("name=\"altText\""))
        assertTrue(body.contains("Retrato"))
        assertTrue(body.contains("name=\"category\""))
        assertTrue(body.contains("portrait"))
    }

    @Test
    fun addMapsConflictAndSizeAndMediaErrors() = runTest {
        val store = InMemorySessionStore().apply { writeAccessToken("token") }
        val cases = listOf(
            409 to MobileAuthErrorCode.CONFLICT,
            413 to MobileAuthErrorCode.INVALID_INPUT,
            415 to MobileAuthErrorCode.INVALID_INPUT
        )
        for ((status, code) in cases) {
            val gateway = HttpPortfolioGateway(FakeHttpTransport(HttpResponse(status)), store)
            try {
                gateway.add(byteArrayOf(1), "image/jpeg", "Alt", "Cap", "portrait")
                throw AssertionError("Expected error for $status")
            } catch (error: MobileAuthException) {
                assertEquals(code, error.code)
            }
        }
    }

    @Test
    fun updateSendsItemsAndParsesPortfolio() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"portfolio":[$itemJson]}"""))
        val store = InMemorySessionStore().apply { writeAccessToken("token") }

        val items = HttpPortfolioGateway(transport, store).update(
            listOf(PortfolioItem(7, "Praia", "Retrato", 0, true, "portrait", ""))
        )

        assertEquals(1, items.size)
        assertEquals("PUT", transport.lastRequest?.method)
        val sent = JSONObject(transport.lastRequest!!.body!!)
        val first = sent.getJSONArray("items").getJSONObject(0)
        assertEquals(7L, first.getLong("id"))
        assertTrue(first.getBoolean("isCover"))
        assertEquals(0, first.getInt("position"))
    }

    @Test
    fun removeSendsDeleteAndMapsNotFound() = runTest {
        val store = InMemorySessionStore().apply { writeAccessToken("token") }
        val okTransport = FakeHttpTransport(HttpResponse(200, body = """{"message":"ok"}"""))

        HttpPortfolioGateway(okTransport, store).remove(7)
        assertEquals("DELETE", okTransport.lastRequest?.method)
        assertEquals("/api/account/portfolio/7", okTransport.lastRequest?.path)

        val gateway = HttpPortfolioGateway(FakeHttpTransport(HttpResponse(404)), store)
        try {
            gateway.remove(99)
            throw AssertionError("Expected NOT_FOUND")
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.NOT_FOUND, error.code)
        }
    }
}