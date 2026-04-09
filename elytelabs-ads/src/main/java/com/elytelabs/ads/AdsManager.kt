package com.elytelabs.ads

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.bumptech.glide.Glide
import com.elytelabs.ads.models.AdModel
import com.elytelabs.ads.models.AdResponse
import com.elytelabs.ads.network.AdsClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * Central manager for the Elyte Labs fallback ad network.
 *
 * Call [init] once (typically in your Activity's `onCreate` or Application class)
 * to fetch and cache promotional ads. The SDK is designed to be used as a **fallback**
 * when primary ad networks (e.g., AdMob) fail to fill.
 *
 * Ads are cached locally for 24 hours and served randomly to maximise
 * cross-promotion coverage. The current app's package name is automatically
 * excluded from the ad pool.
 *
 * Usage:
 * ```kotlin
 * // Initialize the SDK
 * AdsManager.init(context)
 *
 * // Listen for ads to be ready
 * AdsManager.addListener(object : AdsManager.AdLoadListener {
 *     override fun onAdsLoaded() {
 *         bannerAdView.loadAd()
 *     }
 * })
 * ```
 */
object AdsManager {

    private const val TAG = "AdsManager"
    private const val PREFS_NAME = "ElyteAdsCache"
    private const val KEY_CACHED_ADS = "cached_ads_json"
    private const val KEY_LAST_FETCH = "last_fetch_time"
    private val CACHE_DURATION_MS = TimeUnit.HOURS.toMillis(24)

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var cachedAdsList: List<AdModel> = emptyList()

    private val listeners = mutableListOf<WeakReference<AdLoadListener>>()

    /**
     * Returns a randomly selected [AdModel] for banner display.
     * Each access may return a different ad from the cached pool.
     */
    val bannerAdModel: AdModel?
        get() = getRandomAd()

    /**
     * Returns a randomly selected [AdModel] for interstitial display.
     * Each access may return a different ad from the cached pool.
     */
    val interstitialAdModel: AdModel?
        get() = getRandomAd()

    /**
     * Callback interface notified when ads have been loaded and are ready to display.
     */
    interface AdLoadListener {
        /** Called on the main thread when one or more ads are available. */
        fun onAdsLoaded()
    }

    /**
     * Registers a listener for ad load events.
     *
     * If ads are already loaded, the listener is called back immediately.
     * Listeners are held via [WeakReference] to prevent Activity/Fragment memory leaks.
     *
     * @param listener the callback to register
     */
    fun addListener(listener: AdLoadListener) {
        synchronized(listeners) {
            listeners.add(WeakReference(listener))
        }
        if (cachedAdsList.isNotEmpty()) {
            mainHandler.post { listener.onAdsLoaded() }
        }
    }

    /**
     * Unregisters a previously added listener.
     *
     * @param listener the callback to remove
     */
    fun removeListener(listener: AdLoadListener) {
        synchronized(listeners) {
            listeners.removeAll { it.get() == null || it.get() == listener }
        }
    }

    private fun getRandomAd(): AdModel? {
        return cachedAdsList.randomOrNull()
    }

    private fun notifyListeners() {
        val snapshot: List<AdLoadListener>
        synchronized(listeners) {
            listeners.removeAll { it.get() == null }
            snapshot = listeners.mapNotNull { it.get() }
        }
        mainHandler.post {
            snapshot.forEach { it.onAdsLoaded() }
        }
    }

    /**
     * Initialises the Elyte Labs ad SDK.
     *
     * On first call, this will attempt to load ads from the local 24-hour cache.
     * If the cache has expired or is empty, a network request is made to the
     * Elyte Labs promotion API. The current app's package name is automatically
     * excluded from the returned ads.
     *
     * Safe to call multiple times; redundant calls are no-ops while the cache
     * is still valid.
     *
     * @param context any Android context (Activity, Application, etc.)
     */
    fun init(context: Context) {
        val appContext = context.applicationContext

        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastFetchTime = prefs.getLong(KEY_LAST_FETCH, 0L)
        val currentTime = System.currentTimeMillis()
        val cacheValid = currentTime - lastFetchTime < CACHE_DURATION_MS

        // If in-memory list is already populated AND cache is still valid, skip.
        if (cachedAdsList.isNotEmpty() && cacheValid) return

        if (cacheValid) {
            val cachedJson = prefs.getString(KEY_CACHED_ADS, null)
            if (!cachedJson.isNullOrEmpty()) {
                try {
                    val type = object : TypeToken<List<AdModel>>() {}.type
                    val items: List<AdModel> = Gson().fromJson(cachedJson, type)
                    if (items.isNotEmpty()) {
                        cachedAdsList = items
                        preloadIcons(appContext, items)
                        Log.d(TAG, "Loaded ${items.size} ads from 24-hour cache.")
                        notifyListeners()
                        return
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse cached ads.", e)
                }
            }
        }

        fetchAds(appContext)
    }

    private fun fetchAds(appContext: Context) {
        val packageName = appContext.packageName

        AdsClient.api.getAds(
            limit = 50,
            type = "apps",
            exclude = packageName
        ).enqueue(object : Callback<AdResponse> {

            override fun onResponse(call: Call<AdResponse>, response: Response<AdResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val items = response.body()?.items
                    if (!items.isNullOrEmpty()) {
                        cachedAdsList = items
                        preloadIcons(appContext, items)

                        try {
                            val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString(KEY_CACHED_ADS, Gson().toJson(items))
                                .putLong(KEY_LAST_FETCH, System.currentTimeMillis())
                                .apply()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to cache ads.", e)
                        }

                        Log.d(TAG, "Fetched ${items.size} ads from network.")
                        notifyListeners()
                    } else {
                        Log.w(TAG, "API returned empty items list.")
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()} ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AdResponse>, t: Throwable) {
                Log.e(TAG, "Network error fetching ads.", t)
            }
        })
    }

    private fun preloadIcons(context: Context, items: List<AdModel>) {
        items.take(20).forEach { ad ->
            if (!ad.iconUrl.isNullOrEmpty()) {
                Glide.with(context).load(ad.iconUrl).preload()
            }
        }
    }

    /**
     * Returns `true` if at least one ad is available for interstitial display.
     */
    fun isInterstitialLoaded(): Boolean {
        return cachedAdsList.isNotEmpty()
    }

    /**
     * Returns `true` if at least one ad is available for banner display.
     */
    fun isBannerLoaded(): Boolean {
        return cachedAdsList.isNotEmpty()
    }
}
