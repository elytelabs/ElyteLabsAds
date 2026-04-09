package com.elytelabs.ads.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.elytelabs.ads.AdsManager
import com.elytelabs.ads.R
import com.elytelabs.ads.databinding.InterstitialAdBinding
import com.elytelabs.ads.models.AdModel
import jp.wasabeef.glide.transformations.BlurTransformation
import java.util.Locale

/**
 * Full-screen interstitial ad activity.
 *
 * Displays a single ad with a blurred ambient background, app icon, title,
 * description, and a prominent "Install" CTA button.
 *
 * **Google Play Policy compliance:**
 * - Close button is always visible in the top-right corner.
 * - Only the "Install" button navigates to the Play Store.
 * - Clear "Ad" badge labelling.
 *
 * Launch via [show]:
 * ```kotlin
 * if (AdsManager.isInterstitialLoaded()) {
 *     InterstitialAdActivity.show(context)
 * }
 * ```
 */
class InterstitialAdActivity : AppCompatActivity() {

    private lateinit var binding: InterstitialAdBinding
    private var currentAd: AdModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InterstitialAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edge-to-edge support for API 35+
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val adModel = AdsManager.interstitialAdModel
        if (adModel == null) {
            finish()
            return
        }
        currentAd = adModel

        // Populate content
        binding.tvAdTitle.text = adModel.title
        binding.tvAdDescription.text = adModel.description
        binding.btnInstall.text = getString(R.string.install)

        // Rating and Installs meta row
        if (adModel.rating != null || !adModel.installs.isNullOrEmpty()) {
            binding.metaRow.visibility = View.VISIBLE
            binding.tvRating.visibility = if (adModel.rating != null) View.VISIBLE else View.GONE
            binding.tvInstalls.visibility = if (!adModel.installs.isNullOrEmpty()) View.VISIBLE else View.GONE
            
            adModel.rating?.let {
                // Ensure rating is nicely formatted (e.g. 4.5 instead of 4.500000000)
                val roundedRating = String.format(Locale.ROOT,"%.1f", it)
                binding.tvRating.text = roundedRating
            }
            if (!adModel.installs.isNullOrEmpty()) {
                binding.tvInstalls.text = adModel.installs
            }
        } else {
            binding.metaRow.visibility = View.GONE
        }

        // Accessibility
        binding.ivAdImage.contentDescription = adModel.title
        binding.ivAdIcon.contentDescription = getString(R.string.ad_icon_description, adModel.title)
        binding.btnClose.contentDescription = getString(R.string.close_ad)

        // Screen background is ALWAYS the blurred app icon ambient glow
        if (!adModel.iconUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(adModel.iconUrl)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(binding.ivAdImage)
        }

        // Feature Graphic (if available) placed prominently in upper screen
        if (!adModel.featureGraphic.isNullOrEmpty()) {
            binding.ivFeatureGraphic.visibility = View.VISIBLE
            Glide.with(this).load(adModel.featureGraphic).into(binding.ivFeatureGraphic)
        } else {
            binding.ivFeatureGraphic.visibility = View.GONE
        }

        // Crisp app icon
        if (!adModel.iconUrl.isNullOrEmpty()) {
            Glide.with(this).load(adModel.iconUrl).into(binding.ivAdIcon)
        }

        // Apply theme overrides
        AdsManager.theme?.let { theme ->
            theme.buttonColor?.let { binding.btnInstall.background.setTint(it) }
            theme.buttonTextColor?.let { binding.btnInstall.setTextColor(it) }
        }

        // Close button
        binding.btnClose.setOnClickListener { finish() }

        // Install button — the ONLY element that navigates to Play Store
        binding.btnInstall.setOnClickListener {
            AdsManager.adListener?.onAdClicked(adModel)
            openPlayStore(adModel.id)
        }

        // Fire impression callback
        AdsManager.adListener?.onAdImpression(adModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        AdsManager.adListener?.onAdDismissed()
    }

    private fun openPlayStore(packageId: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageId".toUri()))
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageId".toUri()))
        }
        finish()
    }

    companion object {
        /**
         * Launches the interstitial ad activity.
         * @param context the calling Activity or Application context
         */
        fun show(context: Context) {
            context.startActivity(Intent(context, InterstitialAdActivity::class.java))
        }
    }
}
