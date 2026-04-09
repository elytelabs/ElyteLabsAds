package com.elytelabs.ads.models

import com.google.gson.annotations.SerializedName

/**
 * Top-level response from the Elyte Labs promotion API (`/api/promote`).
 *
 * @property success `true` if the API call was handled correctly.
 * @property items   the list of promotional [AdModel] entries, or `null` if none.
 */
data class AdResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("items") val items: List<AdModel>?
)

/**
 * Represents a single promotional ad item.
 *
 * @property id          the app's package name (e.g. `com.elytelabs.muslimnames`).
 * @property iconUrl     URL pointing to the app's icon on Google Play CDN.
 * @property title       the display name of the promoted app.
 * @property description a short promotional tagline.
 * @property url         the full Play Store listing URL (optional).
 */
data class AdModel(
    @SerializedName("id") val id: String,
    @SerializedName("icon") val iconUrl: String?,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("url") val url: String?
)
