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
 * Ads are cached locally (default 24 hours, configurable via [AdsConfig]) and
 * served randomly to maximise cross-promotion coverage. The current app's
 * package name is automatically excluded from the ad pool.
 *
 * Usage:
 * ```kotlin
 * AdsManager.init(context, AdsConfig(cacheHours = 12))
 * AdsManager.adListener = object : AdListener {
 *     override fun onAdClicked(ad: AdModel) { /* analytics */ }
 * }
 * AdsManager.theme = AdsTheme(buttonColor = Color.RED)
 * ```
 */
object AdsManager {

    private const val TAG = "AdsManager"
    private const val PREFS_NAME = "ElyteAdsCache"
    private const val KEY_CACHED_ADS = "cached_ads_json"
    private const val KEY_LAST_FETCH = "last_fetch_time"

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var cachedAdsList: List<AdModel> = emptyList()

    private val listeners = mutableListOf<WeakReference<AdLoadListener>>()

    /** SDK configuration. Set via [init]. */
    var config: AdsConfig = AdsConfig()
        private set

    /**
     * Optional lifecycle listener for analytics and event tracking.
     *
     * ```kotlin
     * AdsManager.adListener = object : AdListener {
     *     override fun onAdImpression(ad: AdModel) { /* log */ }
     *     override fun onAdClicked(ad: AdModel) { /* log */ }
     * }
     * ```
     */
    var adListener: AdListener? = null

    /**
     * Optional color overrides for the SDK's UI components.
     *
     * ```kotlin
     * AdsManager.theme = AdsTheme(buttonColor = Color.parseColor("#FF5722"))
     * ```
     */
    var theme: AdsTheme? = null

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

    /** Callback interface notified when ads are ready to display. */
    interface AdLoadListener {
        /** Called on the main thread when one or more ads are available. */
        fun onAdsLoaded()
    }

    /**
     * Registers a listener for ad load events.
     * Listeners are held via [WeakReference] to prevent Activity leaks.
     * If ads are already loaded, the listener is called back immediately.
     */
    fun addListener(listener: AdLoadListener) {
        synchronized(listeners) {
            listeners.add(WeakReference(listener))
        }
        if (cachedAdsList.isNotEmpty()) {
            mainHandler.post { listener.onAdsLoaded() }
        }
    }

    /** Unregisters a previously added listener. */
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
     * On first call, attempts to load ads from the local cache.
     * If the cache is expired or empty, a network request is made.
     * The current app's package name is automatically excluded.
     *
     * Safe to call multiple times; redundant calls are no-ops while
     * the cache is still valid.
     *
     * @param context any Android context
     * @param config  optional SDK configuration (defaults to [AdsConfig])
     */
    fun init(context: Context, config: AdsConfig = AdsConfig()) {
        this.config = config
        val appContext = context.applicationContext
        val cacheDurationMs = TimeUnit.HOURS.toMillis(config.cacheHours.toLong())

        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastFetchTime = prefs.getLong(KEY_LAST_FETCH, 0L)
        val currentTime = System.currentTimeMillis()
        val cacheValid = currentTime - lastFetchTime < cacheDurationMs

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
                        Log.d(TAG, "Loaded ${items.size} ads from cache.")
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
            limit = config.maxAdsToFetch,
            type = config.adType,
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
                        Log.w(TAG, "API returned empty list.")
                        mainHandler.post { adListener?.onAdFailedToLoad("Empty response") }
                    }
                } else {
                    val msg = "API error: ${response.code()} ${response.message()}"
                    Log.e(TAG, msg)
                    mainHandler.post { adListener?.onAdFailedToLoad(msg) }
                }
            }

            override fun onFailure(call: Call<AdResponse>, t: Throwable) {
                val msg = "Network error: ${t.message}"
                Log.e(TAG, msg, t)
                mainHandler.post { adListener?.onAdFailedToLoad(msg) }
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

    /** Returns `true` if at least one ad is available for interstitial display. */
    fun isInterstitialLoaded(): Boolean = cachedAdsList.isNotEmpty()

    /** Returns `true` if at least one ad is available for banner display. */
    fun isBannerLoaded(): Boolean = cachedAdsList.isNotEmpty()
}
