package com.lensclick.app.data.remote.auth

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Guarda instrumentada do armazenamento de sessão com Android Keystore real:
 * o token só existe cifrado em SharedPreferences e some ao limpar a sessão.
 */
@RunWith(AndroidJUnit4::class)
class AndroidKeystoreSessionStoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val keyAlias = "lensclick.test.session"
    private val store = AndroidKeystoreSessionStore(context, keyAlias)

    @After
    fun tearDown() {
        store.clear()
    }

    @Test
    fun writeThenReadReturnsSameToken() {
        store.writeAccessToken("token-abc-123")
        assertEquals("token-abc-123", store.readAccessToken())
    }

    @Test
    fun readReturnsNullWhenNothingStored() {
        assertNull(store.readAccessToken())
    }

    @Test
    fun clearRemovesStoredToken() {
        store.writeAccessToken("token-to-clear")
        store.clear()
        assertNull(store.readAccessToken())
    }

    @Test
    fun tokenPersistsAcrossStoreInstances() {
        store.writeAccessToken("persisted-token")
        val reopened = AndroidKeystoreSessionStore(context, keyAlias)
        assertEquals("persisted-token", reopened.readAccessToken())
    }

    @Test
    fun overwriteReplacesPreviousToken() {
        store.writeAccessToken("first-token")
        store.writeAccessToken("second-token")
        assertEquals("second-token", store.readAccessToken())
    }
}