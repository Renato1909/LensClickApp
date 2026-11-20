package com.lensclick.app.data.remote

import com.lensclick.app.data.remote.auth.HttpResponse
import com.lensclick.app.data.remote.auth.InMemorySessionStore
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewsGatewayTest {
    private fun store() = InMemorySessionStore().apply { writeAccessToken("token") }

    @Test
    fun getReviewsParsesSummaryAndReviews() = runTest {
        val transport = FakeHttpTransport(
            HttpResponse(
                200,
                body = """{"summary":{"total":5,"average":4.8},"reviews":[{"id":1,"authorName":"Maria","rating":5,"comment":"Excelente trabalho!","createdAt":"2026-11-10"}]}"""
            )
        )
        val gateway = HttpReviewsGateway(transport, store())
        val publicReviews = gateway.getReviews("fotografo-demo")
        assertEquals(5, publicReviews.summary.total)
        assertEquals(4.8, publicReviews.summary.average!!, 0.01)
        assertEquals(1, publicReviews.reviews.size)
        assertEquals("Maria", publicReviews.reviews[0].authorName)
    }

    @Test
    fun submitReviewSendsPostWithRatingAndComment() = runTest {
        val transport = FakeHttpTransport(HttpResponse(201, body = """{"success":true}"""))
        val gateway = HttpReviewsGateway(transport, store())
        val ok = gateway.submitReview("fotografo-demo", 5, "Fotos sensacionais!")
        assertTrue(ok)
        assertEquals("POST", transport.lastRequest?.method)
        assertEquals("/api/photographers/fotografo-demo/reviews", transport.lastRequest?.path)
        val body = JSONObject(transport.lastRequest!!.body!!)
        assertEquals(5, body.getInt("rating"))
        assertEquals("Fotos sensacionais!", body.getString("comment"))
    }
}
