package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountGatewayTest {
    private fun store() = InMemorySessionStore().apply { writeAccessToken("token") }

    @Test
    fun changePasswordReportsIncorrectPasswordAndKeepsCurrentSession() = runTest {
        val transport = FakeHttpTransport(HttpResponse(403))
        val session = store()
        val result = runCatching { HttpAccountGateway(transport, session).changePassword("errada", "SenhaNova2") }
        assertEquals(MobileAuthErrorCode.FORBIDDEN, (result.exceptionOrNull() as? MobileAuthException)?.code)
        assertEquals("token", session.readAccessToken())
    }

    @Test
    fun exportDataReturnsRawJson() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"user":{"id":1,"name":"Test"},"reviews":[],"requests":[]}"""))
        val gateway = HttpAccountGateway(transport, store())
        val export = gateway.exportData()
        assertTrue(export.rawJson.contains("Test"))
        assertEquals("/api/account/export", transport.lastRequest?.path)
    }

    @Test
    fun deleteAccountClearsSessionStoreAndSendsDelete() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"success":true}"""))
        val sessionStore = store()
        val gateway = HttpAccountGateway(transport, sessionStore)
        val ok = gateway.deleteAccount("senha123", "EXCLUIR")
        assertTrue(ok)
        assertEquals("DELETE", transport.lastRequest?.method)
        assertEquals("/api/account", transport.lastRequest?.path)
        val body = JSONObject(transport.lastRequest!!.body!!)
        assertEquals("senha123", body.getString("currentPassword"))
        assertEquals("EXCLUIR", body.getString("confirmation"))
        assertNull(sessionStore.readAccessToken())
    }
}
