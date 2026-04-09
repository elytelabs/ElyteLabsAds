# Elyte Labs Ads SDK

[![](https://jitpack.io/v/elytelabs/elytelabsads.svg)](https://jitpack.io/#elytelabs/elytelabsads)
[![API](https://img.shields.io/badge/API-25%2B-brightgreen.svg)](https://android-arsenal.com/api?level=25)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight fallback ad network SDK for Android. Displays cross-promotional house ads for Elyte Labs apps **only when primary ad providers (AdMob, etc.) fail to fill**.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🎯 **Fallback-First** | Activates only on `onAdFailedToLoad` — never competes with paid ads |
| ⚡ **24-Hour Cache** | Fetches once, serves from disk for 24h (configurable). Works offline |
| 🔀 **Auto-Randomisation** | Every impression pulls a different app from the pool |
| 🚫 **Self-Exclusion** | Automatically excludes the host app from the ad pool |
| 🖼️ **Glide Preloading** | Icons pre-fetched into memory — zero visible latency |
| 🔄 **Banner Auto-Refresh** | Smooth crossfade rotation every 30s (configurable) |
| 📱 **Edge-to-Edge** | Full `WindowInsets` support for Android 15+ gesture nav |
| 🌙 **Dark Mode** | Automatic dark theme via `values-night` resource qualifiers |
| 🎨 **Theming API** | Override button, badge, and background colors programmatically |
| 📊 **Event Callbacks** | `onAdImpression`, `onAdClicked`, `onAdDismissed`, `onAdFailedToLoad` |
| 📋 **4 Ad Formats** | Banner, Interstitial, Native Small, and Native Medium |
| ⚙️ **Configurable** | Cache TTL, refresh interval, ad type, fetch limit — all tuneable |
| ♿ **Accessible** | `contentDescription` on every interactive element |
| 🛡️ **Policy Compliant** | Visible close button, no deceptive clicks, clear "Ad" badge |

---

## 📦 Installation

### Step 1: Add JitPack

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add dependency

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.elytelabs:elytelabsads:1.0.0")
}
```

---

## 🚀 Quick Start

### 1. Initialise

```kotlin
import com.elytelabs.ads.AdsManager
import com.elytelabs.ads.AdsConfig

// Basic (all defaults)
AdsManager.init(this)

// Or with custom config
AdsManager.init(this, AdsConfig(
    cacheHours = 12,
    bannerRefreshSeconds = 45,
    maxAdsToFetch = 30
))
```

### 2. Banner Ad

```xml
<com.elytelabs.ads.ui.BannerAdView
    android:id="@+id/bannerAdView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
AdsManager.addListener(object : AdsManager.AdLoadListener {
    override fun onAdsLoaded() {
        binding.bannerAdView.loadAd()
    }
})
```

> Auto-refreshes with a smooth crossfade every 30s. Interval is configurable via `AdsConfig`.

### 3. Interstitial Ad

```kotlin
if (AdsManager.isInterstitialLoaded()) {
    InterstitialAdActivity.show(this)
}
```

### 4. Native Ad (Small)

Compact inline format. Blends into `RecyclerView` lists — no auto-refresh.

```xml
<com.elytelabs.ads.ui.NativeAdView
    android:id="@+id/nativeAdView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
binding.nativeAdView.loadAd()
```

### 5. Native Ad (Medium)

Card-style format with a blurred hero image, overlapping icon, and full-width CTA. Ideal for content feeds.

```xml
<com.elytelabs.ads.ui.NativeAdMediumView
    android:id="@+id/nativeAdMedium"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
binding.nativeAdMedium.loadAd()
```

### 6. Fallback Pattern (Recommended)

The SDK is designed as an AdMob fallback. Load Elyte ads **only when your primary network has no fill**:

```kotlin
// AdMob banner failed
override fun onAdFailedToLoad(error: LoadAdError) {
    binding.bannerAdView.loadAd()
}

// AdMob interstitial failed
override fun onAdFailedToLoad(error: LoadAdError) {
    if (AdsManager.isInterstitialLoaded()) {
        InterstitialAdActivity.show(this)
    }
}
```

---

## 📊 Event Tracking

```kotlin
import com.elytelabs.ads.AdListener
import com.elytelabs.ads.models.AdModel

AdsManager.adListener = object : AdListener {
    override fun onAdImpression(ad: AdModel) {
        Log.d("Ads", "Impression: ${ad.title}")
    }
    override fun onAdClicked(ad: AdModel) {
        Log.d("Ads", "Click: ${ad.title}")
    }
    override fun onAdDismissed() {
        Log.d("Ads", "Interstitial dismissed")
    }
    override fun onAdFailedToLoad(error: String) {
        Log.e("Ads", "Failed: $error")
    }
}
```

All methods have default no-op implementations — override only what you need.

---

## 🎨 Theming

```kotlin
import com.elytelabs.ads.AdsTheme

AdsManager.theme = AdsTheme(
    buttonColor = Color.parseColor("#FF5722"),
    buttonTextColor = Color.WHITE,
    badgeColor = Color.parseColor("#4CAF50"),
    bannerBackgroundColor = Color.parseColor("#FAFAFA")
)
```

Leave any field `null` to keep the default (auto-adapts to dark/light mode).

---

## ⚙️ Configuration

```kotlin
AdsConfig(
    cacheHours = 24,            // How long cached ads are valid
    bannerRefreshSeconds = 30,  // Banner rotation interval
    adType = "apps",            // "apps", "websites", or "all"
    maxAdsToFetch = 50          // Max items from API (cap: 50)
)
```

---

## 🔄 Lifecycle

```kotlin
class MyActivity : AppCompatActivity() {

    private val adListener = object : AdsManager.AdLoadListener {
        override fun onAdsLoaded() {
            binding.bannerAdView.loadAd()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsManager.init(this)
        AdsManager.addListener(adListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        AdsManager.removeListener(adListener)
    }
}
```

> Listeners are held via `WeakReference` as a safety net, but calling `removeListener` in `onDestroy` is still best practice.

---

## 📁 Example App

See the [`app`](app/) module for a complete working example that demonstrates:
- All 4 ad formats (Banner, Native Small, Native Medium, Interstitial)
- `AdsConfig` custom configuration
- `AdListener` event logging
- `AdsTheme` theming (commented out, ready to enable)
- Proper lifecycle cleanup with `removeListener`
- Edge-to-edge `WindowInsets` handling

---

## 🏗️ Architecture

```
com.elytelabs.ads
├── AdsManager.kt         # Singleton: init, cache, randomisation
├── AdsConfig.kt          # Configuration data class
├── AdsTheme.kt           # Theming data class
├── AdListener.kt         # Event callback interface
├── models/
│   └── AdResponse.kt     # Gson models (AdResponse, AdModel)
├── network/
│   ├── AdsApi.kt         # Retrofit interface
│   └── AdsClient.kt      # Retrofit client (internal)
└── ui/
    ├── BannerAdView.kt           # Auto-refreshing banner
    ├── NativeAdView.kt           # Small inline native ad
    ├── NativeAdMediumView.kt     # Medium card native ad with hero image
    └── InterstitialAdActivity.kt # Full-screen interstitial
```

---

## 🛡️ ProGuard / R8

Ships `consumer-rules.pro` — **automatically applied**. No manual config needed.

---

## 📋 Google Play Policy

| Requirement | How we comply |
|-------------|---------------|
| **Ad labelling** | Yellow "Ad" badge on all formats |
| **Dismissibility** | Prominent ✕ close button on interstitials |
| **No deceptive clicks** | Only "Install" button navigates to Play Store |
| **No unexpected ads** | SDK only shows ads when explicitly called |
| **User data** | No personal data collected |

---

## 📐 Requirements

| | Minimum |
|--|---------|
| Android SDK | API 25+ |
| Kotlin | 2.0+ |
| Gradle | 8.0+ |

---

## 📄 License

MIT License — Copyright (c) 2026 Elyte Labs
