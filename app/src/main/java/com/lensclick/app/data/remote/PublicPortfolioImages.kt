package com.lensclick.app.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MaxPortfolioBytes = 4_000_000
private const val MaxPreviewDimension = 960
private val PublicPortfolioPath = Regex("/api/photographers/[a-z0-9-]{3,80}/portfolio/[0-9]+")

internal suspend fun loadPublicPortfolioPreview(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
    val relativeUrl = try {
        URI.create(imageUrl)
    } catch (_: IllegalArgumentException) {
        return@withContext null
    }
    val path = relativeUrl.rawPath ?: return@withContext null
    if (relativeUrl.isAbsolute || relativeUrl.rawAuthority != null || !PublicPortfolioPath.matches(path)) {
        return@withContext null
    }

    val connection = try {
        URI.create(ApiEnvironment.baseUrl).resolve(relativeUrl).toURL().openConnection() as HttpURLConnection
    } catch (_: IOException) {
        return@withContext null
    }
    try {
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 5_000
        connection.readTimeout = 5_000
        if (connection.responseCode != HttpURLConnection.HTTP_OK ||
            connection.contentType?.startsWith("image/") != true ||
            connection.contentLength > MaxPortfolioBytes
        ) {
            return@withContext null
        }

        val contents = ByteArrayOutputStream()
        connection.inputStream.use { input ->
            val buffer = ByteArray(8_192)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (contents.size() + read > MaxPortfolioBytes) return@withContext null
                contents.write(buffer, 0, read)
            }
        }
        val data = contents.toByteArray()
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(data, 0, data.size, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return@withContext null

        val maxDimension = maxOf(options.outWidth, options.outHeight)
        options.inJustDecodeBounds = false
        options.inSampleSize = 1
        while (maxDimension / options.inSampleSize > MaxPreviewDimension) options.inSampleSize *= 2
        BitmapFactory.decodeByteArray(data, 0, data.size, options)
    } catch (_: IOException) {
        null
    } finally {
        connection.disconnect()
    }
}
