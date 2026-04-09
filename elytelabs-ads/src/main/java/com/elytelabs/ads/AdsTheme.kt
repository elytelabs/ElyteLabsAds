package com.elytelabs.ads

import android.graphics.Color

/**
 * Programmatic color overrides for the ad SDK's UI components.
 *
 * Any non-null value will override the corresponding XML resource color.
 * Leave a field `null` to keep the default (which respects dark mode automatically).
 *
 * ```kotlin
 * AdsManager.theme = AdsTheme(
 *     buttonColor = Color.parseColor("#FF5722"),
 *     badgeColor = Color.parseColor("#4CAF50")
 * )
 * ```
 *
 * @property buttonColor           background color of "Install" buttons.
 * @property buttonTextColor       text color of "Install" buttons.
 * @property badgeColor            background color of the "Ad" badge.
 * @property bannerBackgroundColor background color of the banner ad strip.
 */
data class AdsTheme(
    val buttonColor: Int? = null,
    val buttonTextColor: Int? = null,
    val badgeColor: Int? = null,
    val bannerBackgroundColor: Int? = null
)
