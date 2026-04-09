package com.elytelabs.ads.ui

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.elytelabs.ads.AdsManager
import com.elytelabs.ads.R
import com.elytelabs.ads.databinding.NativeAdBinding

/**
 * A native ad view designed to blend seamlessly into content lists.
 *
 * Unlike [BannerAdView], this view does **not** auto-refresh — it loads once
 * and stays stable, making it ideal for embedding inside `RecyclerView` items.
 *
 * Call [loadAd] from your `onBindViewHolder` or after ads are ready:
 *
 * ```kotlin
 * // In a RecyclerView adapter:
 * override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
 *     holder.nativeAdView.loadAd()
 * }
 * ```
 *
 * XML usage:
 * ```xml
 * <com.elytelabs.ads.ui.NativeAdView
 *     android:id="@+id/nativeAd"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 * ```
 */
class NativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: NativeAdBinding =
        NativeAdBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        visibility = View.GONE
    }

    /**
     * Loads and displays a native ad from the cached ad pool.
     *
     * If no ads are available, the view remains hidden (`View.GONE`).
     * Unlike the banner, this view does **not** auto-refresh.
     */
    fun loadAd() {
        val adModel = AdsManager.bannerAdModel ?: return

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
            theme.bannerBackgroundColor?.let { binding.root.setBackgroundColor(it) }
        }

        val clickListener = OnClickListener {
            AdsManager.adListener?.onAdClicked(adModel)
            openPlayStore(adModel.id)
        }
        binding.root.setOnClickListener(clickListener)
        binding.btnInstall.setOnClickListener(clickListener)
        visibility = View.VISIBLE

        // Fire impression
        AdsManager.adListener?.onAdImpression(adModel)
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
}
