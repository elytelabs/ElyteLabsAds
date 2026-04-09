package com.elytelabs.ads

/**
 * Configuration options for the Elyte Labs ad SDK.
 *
 * Pass an instance to [AdsManager.init] to customise behaviour:
 *
 * ```kotlin
 * AdsManager.init(this, AdsConfig(
 *     cacheHours = 12,
 *     bannerRefreshSeconds = 45,
 *     maxAdsToFetch = 30
 * ))
 * ```
 *
 * @property cacheHours          how long the ad cache is valid (default 24).
 * @property bannerRefreshSeconds interval between banner auto-rotations (default 30).
 * @property adType              API filter: `"apps"`, `"websites"`, or `"all"` (default `"apps"`).
 * @property maxAdsToFetch       max items to request from the API (max 50, default 50).
 */
data class AdsConfig(
    val cacheHours: Int = 24,
    val bannerRefreshSeconds: Int = 30,
    val adType: String = "apps",
    val maxAdsToFetch: Int = 50
)
