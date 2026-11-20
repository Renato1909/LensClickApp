package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TwoFactorGatewayTest {
    private fun store() = InMemorySessionStore().apply { writeAccessToken("token") }

    @Test
    fun getStatusParsesStatus() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"enabled":true,"method":"totp"}"""))
        val gateway = HttpTwoFactorGateway(transport, store())
        val status = gateway.getStatus()
        assertTrue(status.enabled)
        assertEquals("totp", status.method)
        assertEquals("/api/account/2fa/status", transport.lastRequest?.path)
    }

    @Test
    fun setupReturnsSecretAndRecoveryCodes() = runTest {
        val transport = FakeHttpTransport(
            HttpResponse(
                200,
                body = """{"secret":"JBSWY3DPEHPK3PXP","otpauthUrl":"otpauth://totp/LensClick:test?secret=JBSWY3DPEHPK3PXP","recoveryCodes":["12345678","87654321"]}"""
            )
        )
        val gateway = HttpTwoFactorGateway(transport, store())
        val setup = gateway.setup()
        assertEquals("JBSWY3DPEHPK3PXP", setup.secret)
        assertEquals(2, setup.recoveryCodes.size)
        assertEquals("12345678", setup.recoveryCodes[0])
    }

    @Test
    fun enableSendsCodeAndReturnsSuccess() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"success":true}"""))
        val gateway = HttpTwoFactorGateway(transport, store())
        val ok = gateway.enable("123456")
        assertTrue(ok)
        assertEquals("POST", transport.lastRequest?.method)
        assertEquals("123456", JSONObject(transport.lastRequest!!.body!!).getString("code"))
    }

    @Test
    fun disableSendsPasswordAndReturnsSuccess() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"success":true}"""))
        val gateway = HttpTwoFactorGateway(transport, store())
        val ok = gateway.disable("senha123")
        assertTrue(ok)
        assertEquals("senha123", JSONObject(transport.lastRequest!!.body!!).getString("password"))
    }

    @Test
    fun verifyChallengeReturnsSessionAndSavesToken() = runTest {
        val transport = FakeHttpTransport(
            HttpResponse(
                200,
                body = """{"user":{"id":1,"name":"Test","email":"test@example.com","phone":"","role":"client","emailVerified":true,"isAdmin":false},"session":{"accessToken":"new_bearer","expiresAt":"2026-12-31T23:59:59Z"}}"""
            )
        )
        val sessionStore = InMemorySessionStore()
        val gateway = HttpTwoFactorGateway(transport, sessionStore)
        val session = gateway.verifyChallenge("challenge_token", "654321")
        assertEquals("new_bearer", session.accessToken)
        assertEquals("new_bearer", sessionStore.readAccessToken())
        assertEquals("654321", JSONObject(transport.lastRequest!!.body!!).getString("code"))
    }

    @Test
    fun unauthenticatedThrowsException() = runTest {
        val gateway = HttpTwoFactorGateway(FakeHttpTransport(HttpResponse(401)), InMemorySessionStore())
        try {
            gateway.getStatus()
            throw AssertionError("Expected UNAUTHENTICATED")
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.UNAUTHENTICATED, error.code)
        }
    }
}
