package com.example.lensclickapp.data.auth

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileAuthRepositoryTest {
    @Test
    fun loginMapsSuccessStoresTokenAndNeverUsesCookies() = runTest {
        val transport = FakeTransport(HttpResponse(200, body = successBody()))
        val store = InMemorySessionStore()
        val repository = MobileAuthRepository(HttpMobileAuthApi(transport), store)

        val session = repository.login(" User@Example.com ", "Secret123", "Android", "1.0")

        assertEquals(42L, session.user.id)
        assertEquals("access-token", session.accessToken)
        assertEquals("access-token", store.readAccessToken())
        assertEquals("POST", transport.lastRequest?.method)
        assertEquals("/api/mobile/auth/login", transport.lastRequest?.path)
        assertTrue(transport.lastRequest?.headers?.get("Cookie") == null)
        assertTrue(transport.lastRequest?.body?.contains("\"password\":\"Secret123\"") == true)
    }

    @Test
    fun currentUserUsesBearerWithoutCookies() = runTest {
        val transport = FakeTransport(HttpResponse(200, body = userBody()))
        val api = HttpMobileAuthApi(transport)

        val user = api.currentUser("access-token")

        assertEquals("mobile@example.com", user.email)
        assertEquals("Bearer access-token", transport.lastRequest?.headers?.get("Authorization"))
        assertTrue(transport.lastRequest?.headers?.get("Cookie") == null)
        assertEquals("/api/mobile/auth/me", transport.lastRequest?.path)
    }

    @Test
    fun logoutClearsLocalSessionEvenWhenServerFails() = runTest {
        val transport = FakeTransport(HttpResponse(503))
        val store = InMemorySessionStore().apply { writeAccessToken("access-token") }
        val repository = MobileAuthRepository(HttpMobileAuthApi(transport), store)

        try {
            repository.logout()
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.SERVER, error.code)
        }

        assertNull(store.readAccessToken())
        assertEquals("POST", transport.lastRequest?.method)
        assertEquals("/api/mobile/auth/logout", transport.lastRequest?.path)
    }

    @Test
    fun unauthorizedRestoreClearsSession() = runTest {
        val transport = FakeTransport(HttpResponse(401))
        val store = InMemorySessionStore().apply { writeAccessToken("access-token") }
        val repository = MobileAuthRepository(HttpMobileAuthApi(transport), store)

        try {
            repository.restore()
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.UNAUTHENTICATED, error.code)
        }

        assertNull(store.readAccessToken())
    }

    @Test
    fun allDocumentedHttpErrorsMapDeterministically() = runTest {
        val expected = mapOf(
            400 to MobileAuthErrorCode.INVALID_INPUT,
            401 to MobileAuthErrorCode.UNAUTHENTICATED,
            403 to MobileAuthErrorCode.FORBIDDEN,
            404 to MobileAuthErrorCode.NOT_FOUND,
            409 to MobileAuthErrorCode.CONFLICT,
            429 to MobileAuthErrorCode.RATE_LIMITED,
            500 to MobileAuthErrorCode.SERVER,
            503 to MobileAuthErrorCode.SERVER
        )

        expected.forEach { (status, code) ->
            val api = HttpMobileAuthApi(FakeTransport(HttpResponse(status)))
            try {
                api.currentUser("access-token")
            } catch (error: MobileAuthException) {
                assertEquals(status, error.statusCode)
                assertEquals(code, error.code)
            }
        }
    }

    private fun successBody() = """
        {
          "user": {
            "id": 42,
            "name": "Pessoa Mobile",
            "email": "mobile@example.com",
            "phone": "(11) 99999-9999",
            "role": "client",
            "emailVerified": true,
            "isAdmin": false
          },
          "session": {
            "accessToken": "access-token",
            "expiresAt": "2026-10-31T12:00:00.000Z"
          }
        }
    """.trimIndent()

    private fun userBody() = """{"user":{"id":42,"name":"Pessoa Mobile","email":"mobile@example.com","phone":null,"role":"client","emailVerified":false,"isAdmin":false}}"""
}

private class FakeTransport(
    private val response: HttpResponse
) : HttpTransport {
    var lastRequest: HttpRequest? = null

    override suspend fun execute(request: HttpRequest): HttpResponse {
        lastRequest = request
        return response
    }
}
