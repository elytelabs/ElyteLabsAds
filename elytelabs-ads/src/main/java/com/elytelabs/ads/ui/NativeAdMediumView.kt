package com.elytelabs.ads.ui

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elytelabs.ads.AdsManager
import com.elytelabs.ads.R
import com.elytelabs.ads.databinding.NativeAdMediumBinding
import jp.wasabeef.glide.transformations.BlurTransformation

/**
 * A medium-sized native ad view with a featured hero image area.
 *
 * Displays the app icon as a blurred hero banner with a crisp icon overlay,
 * title, description, and Install CTA — similar to AdMob's medium native template.
 *
 * Ideal for content feeds, between-section separators, or standalone placements.
 * Does **not** auto-refresh.
 *
 * ```xml
 * <com.elytelabs.ads.ui.NativeAdMediumView
 *     android:id="@+id/nativeAdMedium"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content" />
 * ```
 */
class NativeAdMediumView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: NativeAdMediumBinding =
        NativeAdMediumBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        visibility = View.GONE
    }

    /**
     * Loads and displays a medium native ad.
     * The hero image is a blurred version of the app icon.
     */
    fun loadAd() {
        val adModel = AdsManager.bannerAdModel ?: return

        binding.tvAdTitle.text = adModel.title
        binding.tvAdDescription.text = adModel.description
        binding.btnInstall.text = context.getString(R.string.install)

        if (!adModel.iconUrl.isNullOrEmpty()) {
            // Blurred hero background
            Glide.with(this)
                .load(adModel.iconUrl)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(binding.ivAdImage)

            // Crisp icon overlay
            Glide.with(this).load(adModel.iconUrl).into(binding.ivAdIcon)
        }

        // Accessibility
        binding.ivAdImage.contentDescription = adModel.title
        binding.ivAdIcon.contentDescription = context.getString(R.string.ad_icon_description, adModel.title)

        // Theme overrides
        AdsManager.theme?.let { theme ->
            theme.buttonColor?.let { binding.btnInstall.background.setTint(it) }
            theme.buttonTextColor?.let { binding.btnInstall.setTextColor(it) }
        }

        val clickListener = OnClickListener {
            AdsManager.adListener?.onAdClicked(adModel)
            openPlayStore(adModel.id)
        }
        binding.btnInstall.setOnClickListener(clickListener)
        visibility = View.VISIBLE

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
