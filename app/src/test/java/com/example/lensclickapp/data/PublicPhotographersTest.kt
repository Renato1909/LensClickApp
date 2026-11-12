package com.example.lensclickapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PublicPhotographersTest {
    @Test
    fun parsesPublishedProfilesAndPaginationWithoutInventingValues() {
        val page = parsePublicPhotographerPage("""
            {
              "photographers": [
                {"slug":"ana-12","name":"Ana Silva","city":"Recife","state":"PE",
                 "specialties":["wedding","portrait"],"startingPriceCents":125000,
                 "ratingAverage":4.75,"reviewCount":8},
                {"slug":"bia-3","name":"Bia","city":"","state":"",
                 "specialties":[],"startingPriceCents":null,"ratingAverage":null,"reviewCount":0}
              ],
              "pagination":{"page":2,"pageSize":12,"total":14,"totalPages":2},
              "sort":"recommended"
            }
        """.trimIndent())

        assertEquals(2, page.page)
        assertEquals(14, page.total)
        assertEquals(2, page.totalPages)
        assertEquals("ana-12", page.photographers[0].slug)
        assertEquals(listOf("wedding", "portrait"), page.photographers[0].specialties)
        assertEquals(125000L, page.photographers[0].startingPriceCents)
        assertEquals(4.75, page.photographers[0].ratingAverage!!, 0.001)
        assertNull(page.photographers[1].startingPriceCents)
        assertNull(page.photographers[1].ratingAverage)
    }

    @Test
    fun acceptsAnEmptyServerPage() {
        val page = parsePublicPhotographerPage("""{"photographers":[],"pagination":{"page":1,"pageSize":12,"total":0,"totalPages":0}}""")
        assertEquals(emptyList<PublicPhotographer>(), page.photographers)
        assertEquals(0, page.totalPages)
    }

    @Test
    fun parsesPublishedProfileWithEmbeddedPortfolioAndOptionalContacts() {
        val profile = parsePublicPhotographerProfile(
            """{"profile":{
              "slug":"ana-12","name":"Ana Silva","bio":"Luz natural","specialties":["portrait"],
              "city":"Recife","state":"PE","serviceRadiusKm":80,"startingPriceCents":125000,
              "availability":"available","ratingAverage":4.5,"reviewCount":2,
              "instagram":"ana","website":"","profilePic":"/api/photographers/ana-12/photo",
              "contacts":{"email":"ana@example.com","phone":""},"isPublished":true,
              "portfolio":[{"id":7,"caption":"Praia","altText":"Retrato na praia","position":1,
                "isCover":true,"category":"portrait","imageUrl":"/api/photographers/ana-12/portfolio/7"}]
            }}"""
        )

        assertEquals("ana-12", profile.profile.slug)
        assertEquals("Ana Silva", profile.profile.name)
        assertEquals("ana@example.com", profile.contacts.email)
        assertEquals(1, profile.portfolio.size)
        assertEquals("/api/photographers/ana-12/portfolio/7", profile.portfolio.single().imageUrl)
    }
}
