package com.example.lensclickapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiEnvironmentTest {
    @Test
    fun debugUsesLocalApiAndNeverProduction() {
        assertEquals("debug", ApiEnvironment.name)
        assertTrue(ApiEnvironment.baseUrl.startsWith("http://10.0.2.2:"))
        assertFalse(ApiEnvironment.baseUrl.contains("workers.dev"))
    }
}
