package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.domain.BudgetRequestDraft
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetsGatewayTest {
    private val requestJson =
        """{"id":7,"eventType":"Casamento","eventDate":"2026-12-10","place":"Campinas, SP","duration":"4 horas","description":"Cerimonia e festa.","additionalInfo":"","status":"proposed","createdAt":"2026-11-14","updatedAt":"2026-11-14","counterpartName":"Fotografa","proposals":[{"id":3,"requestId":7,"priceCents":250000,"conditions":"Inclui ensaio.","status":"pending","createdAt":"2026-11-14"}]}"""

    private fun store() = InMemorySessionStore().apply { writeAccessToken("token") }

    @Test
    fun parseBudgetRequestReadsFieldsAndProposals() {
        val request = parseBudgetRequest(JSONObject(requestJson))
        assertEquals(7L, request.id)
        assertEquals("Casamento", request.eventType)
        assertEquals("proposed", request.status)
        assertEquals("Fotografa", request.counterpartName)
        assertEquals(1, request.proposals.size)
        assertEquals(250000L, request.proposals[0].priceCents)
    }

    @Test
    fun parseBudgetRequestFallsBackToClientName() {
        val request = parseBudgetRequest(JSONObject("""{"id":1,"clientName":"Cliente"}"""))
        assertEquals("Cliente", request.counterpartName)
    }

    @Test
    fun createSendsPostWithBearerAndReturnsId() = runTest {
        val transport = FakeHttpTransport(HttpResponse(201, body = """{"request":{"id":7,"status":"pending"}}"""))
        val id = HttpBudgetRequestsGateway(transport, store()).create(
            "fotografa", BudgetRequestDraft("Casamento", "2026-12-10", "Campinas, SP", "4 horas", "Cerimonia e festa.", "")
        )
        assertEquals(7L, id)
        assertEquals("POST", transport.lastRequest?.method)
        assertEquals("/api/photographers/fotografa/budget-requests", transport.lastRequest?.path)
        assertEquals("Bearer token", transport.lastRequest?.headers?.get("Authorization"))
        assertEquals("2026-12-10", JSONObject(transport.lastRequest!!.body!!).getString("eventDate"))
    }

    @Test
    fun listParsesRequests() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"role":"client","requests":[$requestJson]}"""))
        val requests = HttpBudgetRequestsGateway(transport, store()).list()
        assertEquals(1, requests.size)
        assertEquals("/api/account/budget-requests", transport.lastRequest?.path)
    }

    @Test
    fun detailParsesRequestWithProposals() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"request":$requestJson}"""))
        val request = HttpBudgetRequestsGateway(transport, store()).detail(7)
        assertEquals(7L, request.id)
        assertEquals(1, request.proposals.size)
        assertEquals("/api/account/budget-requests/7", transport.lastRequest?.path)
    }

    @Test
    fun proposeSendsPriceAndReturnsId() = runTest {
        val transport = FakeHttpTransport(HttpResponse(201, body = """{"proposal":{"id":3,"status":"pending"}}"""))
        val id = HttpBudgetRequestsGateway(transport, store()).propose(7, 250000, "Inclui ensaio.")
        assertEquals(3L, id)
        assertEquals("/api/account/budget-requests/7/proposals", transport.lastRequest?.path)
        assertEquals(250000L, JSONObject(transport.lastRequest!!.body!!).getLong("priceCents"))
    }

    @Test
    fun decideSendsAcceptAction() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"request":{"id":7,"status":"accepted"}}"""))
        HttpBudgetRequestsGateway(transport, store()).decide(7, 3, accept = true)
        assertEquals("PUT", transport.lastRequest?.method)
        val sent = JSONObject(transport.lastRequest!!.body!!)
        assertEquals("accept", sent.getString("action"))
        assertEquals(3L, sent.getLong("proposalId"))
    }

    @Test
    fun agendaParsesEvents() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"events":[$requestJson]}"""))
        val events = HttpBudgetRequestsGateway(transport, store()).agenda()
        assertEquals(1, events.size)
        assertEquals("/api/account/agenda", transport.lastRequest?.path)
    }

    @Test
    fun conflictMapsToConflict() = runTest {
        val gateway = HttpBudgetRequestsGateway(FakeHttpTransport(HttpResponse(409)), store())
        try {
            gateway.create("fotografa", BudgetRequestDraft("Casamento", "2026-12-10", "Campinas", "", "Cerimonia e festa.", ""))
            throw AssertionError("Expected CONFLICT")
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.CONFLICT, error.code)
        }
    }

    @Test
    fun favoritesListAddRemove() = runTest {
        val listTransport = FakeHttpTransport(HttpResponse(200, body = """{"favorites":[{"photographerId":9,"slug":"fotografa","name":"Fotografa","createdAt":"2026-11-14"}]}"""))
        val favorites = HttpFavoritesGateway(listTransport, store()).list()
        assertEquals(1, favorites.size)
        assertEquals("fotografa", favorites[0].slug)

        val addTransport = FakeHttpTransport(HttpResponse(201, body = """{"favorite":{"slug":"fotografa"}}"""))
        HttpFavoritesGateway(addTransport, store()).add("fotografa")
        assertEquals("POST", addTransport.lastRequest?.method)
        assertEquals("fotografa", JSONObject(addTransport.lastRequest!!.body!!).getString("slug"))

        val removeTransport = FakeHttpTransport(HttpResponse(200, body = """{"message":"ok"}"""))
        HttpFavoritesGateway(removeTransport, store()).remove(9)
        assertEquals("DELETE", removeTransport.lastRequest?.method)
        assertEquals("/api/account/favorites/9", removeTransport.lastRequest?.path)
    }

    @Test
    fun withoutTokenIsUnauthenticated() = runTest {
        val gateway = HttpBudgetRequestsGateway(FakeHttpTransport(HttpResponse(200)), InMemorySessionStore())
        try {
            gateway.list()
            throw AssertionError("Expected UNAUTHENTICATED")
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.UNAUTHENTICATED, error.code)
        }
    }
}