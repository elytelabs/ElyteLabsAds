package com.elytelabs.ads.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
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
 * and call [loadAd] once the [AdsManager] has finished loading.
 *
 * The banner auto-refreshes (default 30s, configurable via [com.elytelabs.ads.AdsConfig])
 * with a smooth crossfade transition.
 *
 * ```xml
 * <com.elytelabs.ads.ui.BannerAdView
 *     android:id="@+id/bannerAdView"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
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
            refreshHandler.postDelayed(this, refreshIntervalMs)
        }
    }

    private val refreshIntervalMs: Long
        get() = AdsManager.config.bannerRefreshSeconds * 1000L

    private var isFirstLoad = true

    init {
        visibility = View.GONE
    }

    /**
     * Loads and displays a banner ad from the cached ad pool.
     *
     * If no ads are available, the view remains hidden (`View.GONE`).
     * Subsequent calls trigger a smooth crossfade transition.
     */
    fun loadAd() {
        val adModel = AdsManager.bannerAdModel ?: return

        if (isFirstLoad) {
            // First load — show immediately
            bindAd(adModel)
            isFirstLoad = false
        } else {
            // Subsequent loads — crossfade
            animate().alpha(0f).setDuration(FADE_DURATION_MS).withEndAction {
                bindAd(adModel)
                animate().alpha(1f).setDuration(FADE_DURATION_MS).start()
            }.start()
        }
    }

    private fun bindAd(adModel: com.elytelabs.ads.models.AdModel) {
        binding.tvAdTitle.text = adModel.title
        binding.tvAdDescription.text = adModel.description
        binding.btnInstall.text = context.getString(R.string.install)

        if (!adModel.iconUrl.isNullOrEmpty()) {
            Glide.with(this).load(adModel.iconUrl).into(binding.ivAdIcon)
        }

        // Apply theme overrides
        AdsManager.theme?.let { theme ->
            theme.buttonColor?.let { binding.btnInstall.background.setTint(it) }
            theme.buttonTextColor?.let { binding.btnInstall.setTextColor(it) }
            theme.bannerBackgroundColor?.let {
                binding.root.setBackgroundColor(it)
            }
            theme.badgeColor?.let { color ->
                binding.root.findViewWithTag<View>("ad_badge")?.let { badge ->
                    (badge.background as? GradientDrawable)?.setColor(color)
                }
            }
        }

        val clickListener = OnClickListener {
            AdsManager.adListener?.onAdClicked(adModel)
            openPlayStore(adModel.id)
        }
        binding.root.setOnClickListener(clickListener)
        binding.btnInstall.setOnClickListener(clickListener)
        visibility = View.VISIBLE

        // Fire impression callback
        AdsManager.adListener?.onAdImpression(adModel)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        refreshHandler.postDelayed(refreshRunnable, refreshIntervalMs)
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
        private const val FADE_DURATION_MS = 150L
    }
}
