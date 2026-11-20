package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationsGatewayTest {
    private fun store() = InMemorySessionStore().apply { writeAccessToken("token") }

    @Test
    fun listConversationsParsesList() = runTest {
        val transport = FakeHttpTransport(
            HttpResponse(
                200,
                body = """{"conversations":[{"id":10,"counterpartName":"Fotógrafo Teste","lastMessage":"Olá!","lastMessageAt":"2026-11-15T10:00:00Z","unreadCount":2}]}"""
            )
        )
        val gateway = HttpConversationsGateway(transport, store())
        val list = gateway.list()
        assertEquals(1, list.size)
        assertEquals(10L, list[0].id)
        assertEquals("Fotógrafo Teste", list[0].counterpartName)
        assertEquals(2, list[0].unreadCount)
    }

    @Test
    fun getMessagesParsesMessages() = runTest {
        val transport = FakeHttpTransport(
            HttpResponse(
                200,
                body = """{"messages":[{"id":100,"senderId":1,"text":"Tudo bem?","createdAt":"2026-11-15T10:01:00Z","isMine":true}]}"""
            )
        )
        val gateway = HttpConversationsGateway(transport, store())
        val msgs = gateway.getMessages(10)
        assertEquals(1, msgs.size)
        assertEquals(100L, msgs[0].id)
        assertEquals("Tudo bem?", msgs[0].text)
        assertTrue(msgs[0].isMine)
    }

    @Test
    fun sendMessageSendsPostAndReturnsMessage() = runTest {
        val transport = FakeHttpTransport(
            HttpResponse(
                201,
                body = """{"message":{"id":101,"senderId":1,"text":"Sim!","createdAt":"2026-11-15T10:02:00Z","isMine":true}}"""
            )
        )
        val gateway = HttpConversationsGateway(transport, store())
        val msg = gateway.sendMessage(10, "Sim!")
        assertEquals(101L, msg.id)
        assertEquals("Sim!", msg.text)
        assertEquals("POST", transport.lastRequest?.method)
        assertEquals("Sim!", JSONObject(transport.lastRequest!!.body!!).getString("message"))
    }

    @Test
    fun startConversationSendsPost() = runTest {
        val transport = FakeHttpTransport(
            HttpResponse(
                201,
                body = """{"conversation":{"id":12,"counterpartName":"Fotógrafo","lastMessage":"Oi","lastMessageAt":"2026-11-15T10:03:00Z","unreadCount":0}}"""
            )
        )
        val gateway = HttpConversationsGateway(transport, store())
        val conv = gateway.startConversation("fotografo-slug", "Oi")
        assertEquals(12L, conv.id)
        assertEquals("/api/photographers/fotografo-slug/conversations", transport.lastRequest?.path)
    }

    @Test
    fun markAsReadSendsPut() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"success":true}"""))
        val gateway = HttpConversationsGateway(transport, store())
        gateway.markAsRead(10)
        assertEquals("PUT", transport.lastRequest?.method)
        assertEquals("/api/account/conversations/10/read", transport.lastRequest?.path)
    }
}
