package com.lensclick.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TotpQrCodeTest {
    private val otpauth = "otpauth://totp/LensClick:demo@example.com?issuer=LensClick&digits=6&period=30"

    @Test
    fun `qr code eh deterministico e nao vazio`() {
        val first = TotpQrCode.modules(otpauth, 256)
        val second = TotpQrCode.modules(otpauth, 256)

        assertEquals(256 * 256, first.size)
        assertTrue(first.contentEquals(second))
        assertTrue(first.count { it } > 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `conteudo vazio nao gera qr code`() {
        TotpQrCode.modules(" ")
    }
}
