package com.lensclick.app.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Gera o QR Code do otpauth:// localmente: o segredo nunca sai do dispositivo. */
object TotpQrCode {
    fun modules(content: String, size: Int = 512): BooleanArray {
        require(content.isNotBlank()) { "Conteudo vazio nao gera QR Code." }
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val result = BooleanArray(matrix.width * matrix.height)
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) result[y * matrix.width + x] = matrix[x, y]
        }
        return result
    }

    fun bitmap(content: String, size: Int = 512): Bitmap {
        val pixels = modules(content, size)
        val width = kotlin.math.sqrt(pixels.size.toDouble()).toInt()
        val colors = IntArray(pixels.size) { index ->
            if (pixels[index]) 0xFF1E2128.toInt() else 0xFFFFFFFF.toInt()
        }
        return Bitmap.createBitmap(colors, width, width, Bitmap.Config.ARGB_8888)
    }
}
