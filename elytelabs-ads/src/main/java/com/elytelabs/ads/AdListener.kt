package com.elytelabs.ads

import com.elytelabs.ads.models.AdModel

/**
 * Callback interface for ad lifecycle events.
 *
 * All methods have default no-op implementations so you only need
 * to override the ones you care about.
 *
 * ```kotlin
 * AdsManager.adListener = object : AdListener {
 *     override fun onAdImpression(ad: AdModel) {
 *         analytics.logEvent("ad_impression", bundleOf("ad_id" to ad.id))
 *     }
 *     override fun onAdClicked(ad: AdModel) {
 *         analytics.logEvent("ad_click", bundleOf("ad_id" to ad.id))
 *     }
 * }
 * ```
 */
interface AdListener {

    /** Called when an ad becomes visible to the user (banner displayed or interstitial opened). */
    fun onAdImpression(ad: AdModel) {}

    /** Called when the user taps the "Install" button. */
    fun onAdClicked(ad: AdModel) {}

    /** Called when the interstitial ad is dismissed (close button or back press). */
    fun onAdDismissed() {}

    /** Called when ads fail to load due to network error or empty response. */
    fun onAdFailedToLoad(error: String) {}
}
