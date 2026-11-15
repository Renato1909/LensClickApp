package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import com.lensclick.app.data.remote.auth.MobileAuthErrorCode
import com.lensclick.app.data.remote.auth.MobileAuthException
import com.lensclick.app.domain.ProfessionalProfile
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessionalProfileGatewayTest {
    @Test
    fun parseReadsOwnerViewFields() {
        val profile = parseProfessionalProfile(
            """{"profile":{
              "slug":"ana-12","bio":"Luz natural","specialties":["portrait","wedding"],
              "city":"Recife","state":"PE","serviceRadiusKm":80,"startingPriceCents":125000,
              "availability":"limited","ratingAverage":4.5,"reviewCount":2,
              "instagram":"ana","website":"https://ana.example.com",
              "publicEmail":"ana@example.com","publicPhone":"(81) 99999-9999",
              "showEmail":true,"showPhone":false,"isPublished":true}}"""
        )
        assertEquals("ana-12", profile.slug)
        assertEquals(listOf("portrait", "wedding"), profile.specialties)
        assertEquals(80, profile.serviceRadiusKm)
        assertEquals(125000L, profile.startingPriceCents)
        assertEquals("limited", profile.availability)
        assertEquals(4.5, profile.ratingAverage!!, 0.001)
        assertEquals(2, profile.reviewCount)
        assertEquals("ana", profile.instagram)
        assertEquals("https://ana.example.com", profile.website)
        assertEquals("ana@example.com", profile.publicEmail)
        assertTrue(profile.showEmail)
        assertFalse(profile.showPhone)
        assertTrue(profile.isPublished)
    }

    @Test
    fun parseHandlesNullOptionalFields() {
        val profile = parseProfessionalProfile("""{"profile":{"slug":"x","specialties":[]}}""")
        assertNull(profile.serviceRadiusKm)
        assertNull(profile.startingPriceCents)
        assertNull(profile.ratingAverage)
        assertEquals("available", profile.availability)
        assertFalse(profile.isPublished)
    }

    @Test
    fun loadSendsBearerGetAndParsesProfile() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"profile":{"slug":"ana-12","bio":"Ok"}}"""))
        val store = InMemorySessionStore().apply { writeAccessToken("token") }

        val profile = HttpProfessionalProfileGateway(transport, store).load()

        assertEquals("ana-12", profile.slug)
        assertEquals("GET", transport.lastRequest?.method)
        assertEquals("/api/account/professional-profile", transport.lastRequest?.path)
        assertEquals("Bearer token", transport.lastRequest?.headers?.get("Authorization"))
    }

    @Test
    fun loadWithoutTokenIsUnauthenticated() = runTest {
        val gateway = HttpProfessionalProfileGateway(FakeHttpTransport(HttpResponse(200)), InMemorySessionStore())
        try {
            gateway.load()
            throw AssertionError("Expected UNAUTHENTICATED")
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.UNAUTHENTICATED, error.code)
        }
    }

    @Test
    fun saveSendsPutWithEditableFields() = runTest {
        val transport = FakeHttpTransport(HttpResponse(200, body = """{"profile":{"slug":"ana-12","isPublished":true}}"""))
        val store = InMemorySessionStore().apply { writeAccessToken("token") }

        val result = HttpProfessionalProfileGateway(transport, store).save(
            ProfessionalProfile(
                bio = "Luz natural", specialties = listOf("portrait"), city = "Recife", state = "PE",
                publicEmail = "ana@example.com", showEmail = true, isPublished = true
            )
        )

        assertTrue(result.isPublished)
        assertEquals("PUT", transport.lastRequest?.method)
        assertEquals("application/json", transport.lastRequest?.headers?.get("Content-Type"))
        val sent = JSONObject(transport.lastRequest!!.body!!)
        assertEquals("Luz natural", sent.getString("bio"))
        assertEquals("PE", sent.getString("state"))
        assertEquals("ana@example.com", sent.getString("publicEmail"))
        assertTrue(sent.getBoolean("showEmail"))
        assertTrue(sent.getBoolean("isPublished"))
        assertEquals("portrait", sent.getJSONArray("specialties").getString(0))
    }

    @Test
    fun saveMapsPublicationAndRateLimitErrors() = runTest {
        val store = InMemorySessionStore().apply { writeAccessToken("token") }
        val cases = listOf(
            400 to MobileAuthErrorCode.INVALID_INPUT,
            403 to MobileAuthErrorCode.FORBIDDEN,
            429 to MobileAuthErrorCode.RATE_LIMITED
        )
        for ((status, code) in cases) {
            val gateway = HttpProfessionalProfileGateway(FakeHttpTransport(HttpResponse(status)), store)
            try {
                gateway.save(ProfessionalProfile())
                throw AssertionError("Expected error for $status")
            } catch (error: MobileAuthException) {
                assertEquals(code, error.code)
                assertEquals(status, error.statusCode)
            }
        }
    }

    @Test
    fun malformedResponseMapsToMalformed() = runTest {
        val store = InMemorySessionStore().apply { writeAccessToken("token") }
        val gateway = HttpProfessionalProfileGateway(FakeHttpTransport(HttpResponse(200, body = "{bad")), store)
        try {
            gateway.load()
            throw AssertionError("Expected MALFORMED_RESPONSE")
        } catch (error: MobileAuthException) {
            assertEquals(MobileAuthErrorCode.MALFORMED_RESPONSE, error.code)
        }
    }
}