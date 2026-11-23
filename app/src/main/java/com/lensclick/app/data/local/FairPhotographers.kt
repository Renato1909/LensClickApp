package com.lensclick.app.data.local

import android.content.Context
import androidx.annotation.DrawableRes
import com.lensclick.app.R
import com.lensclick.app.domain.PublicPhotographer
import org.json.JSONArray

internal data class FairPhotographer(
    val summary: PublicPhotographer,
    val bio: String,
    val photos: List<FairPhoto>
)

internal data class FairPhoto(
    @get:DrawableRes val drawable: Int,
    val caption: String,
    val altText: String
)

private val PortfolioDrawables = mapOf(
    "card_wedding.jpg" to R.drawable.card_wedding,
    "card_portrait_studio.jpg" to R.drawable.card_portrait_studio,
    "card_portrait_outdoor.jpg" to R.drawable.card_portrait_outdoor,
    "card_corporate.jpg" to R.drawable.card_corporate,
    "card_event.jpg" to R.drawable.card_event
)

internal fun readFairPhotographers(context: Context): List<FairPhotographer> {
    val catalog = context.assets.open("feira/photographers.json").bufferedReader().use { JSONArray(it.readText()) }
    return List(catalog.length()) { index ->
        val row = catalog.getJSONObject(index)
        val specialties = row.getJSONArray("specialties")
        val photos = row.getJSONArray("photos")
        FairPhotographer(
            summary = PublicPhotographer(
                slug = row.getString("slug"),
                name = row.getString("name"),
                city = row.getString("city"),
                state = "SP",
                specialties = List(specialties.length(), specialties::getString),
                startingPriceCents = row.getLong("price"),
                ratingAverage = null,
                reviewCount = 0
            ),
            bio = row.getString("bio"),
            photos = List(photos.length()) { photoIndex ->
                val photo = photos.getJSONArray(photoIndex)
                FairPhoto(
                    drawable = PortfolioDrawables.getValue(photo.getString(0)),
                    caption = photo.getString(1),
                    altText = photo.getString(2)
                )
            }
        )
    }
}
