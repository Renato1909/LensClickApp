package com.lensclick.app.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Codifica a URL do autenticador no dispositivo para exibir o QR no app. */
object TotpQrCode {
    internal fun matrix(content: String, size: Int): BitMatrix {
        require(content.startsWith("otpauth://totp/"))
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
        )
        return QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    }

    fun bitmap(content: String, size: Int = 512): Bitmap {
        val matrix = matrix(content, size)
        val colors = IntArray(matrix.width * matrix.height) { index ->
            if (matrix[index % matrix.width, index / matrix.width]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
        return Bitmap.createBitmap(colors, matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    }
}
