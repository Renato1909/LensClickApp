package com.lensclick.app.data.local

import android.content.Context
import androidx.annotation.DrawableRes
import com.lensclick.app.R
import com.lensclick.app.domain.PublicPhotographer
import com.lensclick.app.ui.localized
import org.json.JSONArray

internal data class FairPhotographer(
    val summary: PublicPhotographer,
    @get:DrawableRes val portrait: Int,
    val bio: String,
    val photos: List<FairPhoto>,
    val reviews: List<FairReview>
)

internal data class FairPhoto(
    @get:DrawableRes val drawable: Int,
    val caption: String,
    val altText: String
)

internal data class FairReview(val name: String, val rating: Int, val comment: String)

private val PortraitDrawables = mapOf(
    "feira-livia-monteiro" to R.drawable.discovery_livia,
    "feira-rafael-sato" to R.drawable.discovery_rafael,
    "feira-joana-nascimento" to R.drawable.discovery_joana,
    "feira-bruno-vidal" to R.drawable.discovery_bruno
)

private val PortfolioDrawables = mapOf(
    "card_wedding.jpg" to R.drawable.card_wedding,
    "card_portrait_studio.jpg" to R.drawable.card_portrait_studio,
    "card_portrait_outdoor.jpg" to R.drawable.card_portrait_outdoor,
    "card_corporate.jpg" to R.drawable.card_corporate,
    "card_event.jpg" to R.drawable.card_event,
    "discovery_wedding.webp" to R.drawable.discovery_wedding,
    "discovery_portrait.webp" to R.drawable.discovery_portrait,
    "discovery_events.webp" to R.drawable.discovery_events
)

internal fun readFairPhotographers(context: Context): List<FairPhotographer> {
    val catalog = context.assets.open("feira/photographers.json").bufferedReader().use { JSONArray(it.readText()) }
    return List(catalog.length()) { index ->
        val row = catalog.getJSONObject(index)
        val slug = row.getString("slug")
        val specialties = row.getJSONArray("specialties")
        val photos = row.getJSONArray("photos")
        val reviews = row.getJSONArray("reviews")
        FairPhotographer(
            summary = PublicPhotographer(
                slug = slug,
                name = row.getString("name"),
                city = row.getString("city"),
                state = "SP",
                specialties = List(specialties.length(), specialties::getString),
                startingPriceCents = row.getLong("price"),
                ratingAverage = null,
                reviewCount = 0
            ),
            portrait = PortraitDrawables.getValue(slug),
            bio = localized(row.getString("bio")),
            photos = List(photos.length()) { photoIndex ->
                val photo = photos.getJSONArray(photoIndex)
                FairPhoto(
                    drawable = PortfolioDrawables.getValue(photo.getString(0)),
                    caption = localized(photo.getString(1)),
                    altText = localized(photo.getString(2))
                )
            },
            reviews = List(reviews.length()) { reviewIndex ->
                val review = reviews.getJSONArray(reviewIndex)
                FairReview(review.getString(0), review.getInt(1), review.getString(2))
            }
        )
    }
}
