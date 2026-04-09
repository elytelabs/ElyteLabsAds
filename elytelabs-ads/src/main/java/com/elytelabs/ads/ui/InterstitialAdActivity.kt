package com.elytelabs.ads.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import jp.wasabeef.glide.transformations.BlurTransformation

/**
 * Full-screen interstitial ad activity.
 *
 * This activity displays a single cross-promotional ad with a blurred ambient
 * background, app icon, title, description, and a prominent "Install" CTA button.
 *
 * **Google Play Policy compliance:**
 * - A clearly visible close button is always present in the top-right corner.
 * - Only the "Install" button navigates to the Play Store; tapping elsewhere
 *   does **not** trigger navigation (no deceptive click areas).
 * - The ad is clearly labelled with an "Ad" badge.
 *
 * Launch via the companion [show] helper:
 * ```kotlin
 * if (AdsManager.isInterstitialLoaded()) {
 *     InterstitialAdActivity.show(context)
 * }
 * ```
 */
class InterstitialAdActivity : AppCompatActivity() {

    private lateinit var binding: InterstitialAdBinding

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

        // Populate ad content
        binding.tvAdTitle.text = adModel.title
        binding.tvAdDescription.text = adModel.description
        binding.btnInstall.text = getString(R.string.install)

        // Accessibility
        binding.ivAdImage.contentDescription = adModel.title
        binding.ivAdIcon.contentDescription = getString(R.string.ad_icon_description, adModel.title)
        binding.btnClose.contentDescription = getString(R.string.close_ad)

        // Load blurred ambient background + crisp icon
        if (!adModel.iconUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(adModel.iconUrl)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3)))
                .into(binding.ivAdImage)

            Glide.with(this)
                .load(adModel.iconUrl)
                .into(binding.ivAdIcon)
        }

        // Close button — must always work per Google Play ad policy
        binding.btnClose.setOnClickListener { finish() }

        // Only the Install button navigates to Play Store.
        // Per Google Play policy, non-interactive areas must NOT trigger navigation.
        binding.btnInstall.setOnClickListener {
            openPlayStore(adModel.id)
        }
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
         *
         * @param context the calling Activity or Application context
         */
        fun show(context: Context) {
            context.startActivity(Intent(context, InterstitialAdActivity::class.java))
        }
    }
}
