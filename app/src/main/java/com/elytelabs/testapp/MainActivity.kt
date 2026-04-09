package com.elytelabs.testapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.elytelabs.ads.AdListener
import com.elytelabs.ads.AdsConfig
import com.elytelabs.ads.AdsManager
import com.elytelabs.ads.models.AdModel
import com.elytelabs.ads.ui.InterstitialAdActivity
import com.elytelabs.testapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adLoadListener = object : AdsManager.AdLoadListener {
        override fun onAdsLoaded() {
            binding.bannerAdView.loadAd()
            binding.nativeAdView.loadAd()
            binding.nativeAdMediumView.loadAd()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        // Initialise with custom config
        AdsManager.init(this, AdsConfig(
            cacheHours = 24,
            bannerRefreshSeconds = 30,
            adType = "apps",
            maxAdsToFetch = 50
        ))

        // Optional: custom theme
        // AdsManager.theme = AdsTheme(
        //     buttonColor = Color.parseColor("#FF5722"),
        //     buttonTextColor = Color.WHITE
        // )

        // Event tracking
        AdsManager.adListener = object : AdListener {
            override fun onAdImpression(ad: AdModel) {
                Log.d(TAG, "Impression: ${ad.title}")
            }
            override fun onAdClicked(ad: AdModel) {
                Log.d(TAG, "Click: ${ad.title}")
            }
            override fun onAdDismissed() {
                Log.d(TAG, "Interstitial dismissed")
            }
            override fun onAdFailedToLoad(error: String) {
                Log.e(TAG, "Failed to load: $error")
            }
        }

        // Load all ad formats when ready
        AdsManager.addListener(adLoadListener)

        // Interstitial button
        binding.btnShowInterstitial.setOnClickListener {
            if (AdsManager.isInterstitialLoaded()) {
                InterstitialAdActivity.show(this)
            } else {
                Toast.makeText(this, getString(R.string.interstitial_not_loaded), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AdsManager.removeListener(adLoadListener)
    }

    companion object {
        private const val TAG = "AdsTest"
    }
}
