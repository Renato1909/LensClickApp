package com.lensclick.app.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessionalProfileTest {
    @Test
    fun defaultProfileIsAnUnpublishedDraft() {
        val profile = ProfessionalProfile()
        assertFalse(profile.isPublished)
        assertEquals("", profile.bio)
        assertTrue(profile.specialties.isEmpty())
        assertFalse(profile.showEmail)
        assertFalse(profile.showPhone)
        assertEquals("available", profile.availability)
    }

    @Test
    fun editingPreservesUnrelatedFieldsAndPublicationState() {
        val draft = ProfessionalProfile(
            bio = "Luz natural",
            specialties = listOf("portrait"),
            city = "Recife",
            state = "PE",
            startingPriceCents = 125000
        )
        val published = draft.copy(isPublished = true)

        assertTrue(published.isPublished)
        assertEquals("Luz natural", published.bio)
        assertEquals(listOf("portrait"), published.specialties)
        assertEquals(125000L, published.startingPriceCents)
        assertFalse(draft.isPublished)
    }

    @Test
    fun gatewayLoadReturnsLastSavedProfile() = runTest {
        val gateway = FakeProfessionalProfileGateway()
        val draft = ProfessionalProfile(bio = "Luz natural", city = "Recife", state = "PE")

        gateway.save(draft)

        assertEquals(draft, gateway.load())
        assertFalse(gateway.load().isPublished)
    }
}

private class FakeProfessionalProfileGateway(
    initial: ProfessionalProfile = ProfessionalProfile()
) : ProfessionalProfileGateway {
    private var current = initial

    override suspend fun load(): ProfessionalProfile = current

    override suspend fun save(profile: ProfessionalProfile): ProfessionalProfile {
        current = profile
        return current
    }
}