package com.elytelabs.ads.ui

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.elytelabs.ads.AdsManager
import com.elytelabs.ads.R
import com.elytelabs.ads.databinding.BannerAdBinding

/**
 * A drop-in banner ad view that displays a cross-promotional Elyte Labs ad.
 *
 * Place this view in your layout XML (typically anchored to the bottom of the screen)
 * and call [loadAd] once the [AdsManager] has finished loading ads.
 *
 * The banner automatically refreshes every 30 seconds to show a different
 * randomly selected ad from the cached pool.
 *
 * Example XML:
 * ```xml
 * <com.elytelabs.ads.ui.BannerAdView
 *     android:id="@+id/bannerAdView"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 * ```
 *
 * Example Kotlin (fallback usage):
 * ```kotlin
 * override fun onAdFailedToLoad(error: LoadAdError) {
 *     bannerAdView.loadAd()
 * }
 * ```
 */
class BannerAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: BannerAdBinding =
        BannerAdBinding.inflate(LayoutInflater.from(context), this, true)

    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            loadAd()
            refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS)
        }
    }

    init {
        visibility = View.GONE
    }

    /**
     * Loads and displays a banner ad from the cached ad pool.
     *
     * If no ads are available, the view remains hidden (`View.GONE`).
     * The banner will automatically refresh every 30 seconds after
     * the first successful load.
     */
    fun loadAd() {
        val adModel = AdsManager.bannerAdModel ?: return

        binding.tvAdTitle.text = adModel.title
        binding.tvAdDescription.text = adModel.description
        binding.btnInstall.text = context.getString(R.string.install)

        if (!adModel.iconUrl.isNullOrEmpty()) {
            Glide.with(this).load(adModel.iconUrl).into(binding.ivAdIcon)
        }

        val clickListener = OnClickListener {
            openPlayStore(adModel.id)
        }
        binding.root.setOnClickListener(clickListener)
        binding.btnInstall.setOnClickListener(clickListener)
        visibility = View.VISIBLE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        refreshHandler.removeCallbacks(refreshRunnable)
    }

    private fun openPlayStore(packageName: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
            )
        } catch (_: Exception) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri())
            )
        }
    }

    companion object {
        /** Banner refresh interval in milliseconds (30 seconds). */
        private const val REFRESH_INTERVAL_MS = 30_000L
    }
}
