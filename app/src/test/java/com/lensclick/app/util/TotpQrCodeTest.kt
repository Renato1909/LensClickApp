package com.lensclick.app.util

import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Test

class TotpQrCodeTest {
    private val url = "otpauth://totp/LensClick:demo%40example.com?secret=JBSWY3DPEHPK3PXP&issuer=LensClick"

    @Test
    fun qrCodeRoundTripsAuthenticatorUrl() {
        val matrix = TotpQrCode.matrix(url, 256)
        val pixels = IntArray(matrix.width * matrix.height) { index ->
            if (matrix[index % matrix.width, index / matrix.width]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
        val image = RGBLuminanceSource(matrix.width, matrix.height, pixels)

        assertEquals(url, QRCodeReader().decode(BinaryBitmap(HybridBinarizer(image))).text)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsEmptyAuthenticatorUrl() {
        TotpQrCode.matrix(" ", 256)
    }
}
