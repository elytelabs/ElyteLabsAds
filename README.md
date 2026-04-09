# Elyte Labs Ads SDK

[![](https://jitpack.io/v/ElyteLabs/ElyteLabsAds.svg)](https://jitpack.io/#ElyteLabs/ElyteLabsAds)
[![API](https://img.shields.io/badge/API-25%2B-brightgreen.svg)](https://android-arsenal.com/api?level=25)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight, zero-dependency fallback ad network SDK for Android. Designed to display cross-promotional house ads for Elyte Labs applications **only when primary ad providers (AdMob, etc.) fail to fill**.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🎯 **Fallback-First** | Activates only on `onAdFailedToLoad` — never competes with paid ads. |
| ⚡ **24-Hour Cache** | Fetches once, serves instantly from disk for 24h (configurable). Works offline. |
| 🔀 **Auto-Randomisation** | Every impression pulls a different app from the pool. |
| 🚫 **Self-Exclusion** | Automatically excludes the host app from the ad pool. |
| 🖼️ **Glide Preloading** | Icons pre-fetched into memory — zero visible latency. |
| 🔄 **Banner Auto-Refresh** | Rotates with smooth crossfade every 30s (configurable). |
| 📱 **Edge-to-Edge** | Full `WindowInsets` support for Android 15+ gesture nav. |
| 🌙 **Dark Mode** | Automatic dark theme via `values-night` resource qualifiers. |
| 🎨 **Theming API** | Override button, badge, and background colors programmatically. |
| 📊 **Event Callbacks** | `onAdImpression`, `onAdClicked`, `onAdDismissed`, `onAdFailedToLoad`. |
| 📋 **Native Ads** | Embeddable `NativeAdView` for RecyclerView lists. |
| ⚙️ **Configurable** | Cache TTL, refresh interval, ad type, and fetch limit — all tuneable. |
| ♿ **Accessible** | `contentDescription` on every interactive element. |
| 🛡️ **Policy Compliant** | Visible close button, no deceptive clicks, clear "Ad" badge. |

---

## 📦 Installation

### Step 1: Add JitPack repository

In your root `settings.gradle.kts`:

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

### Step 2: Add the dependency

```kotlin
dependencies {
    implementation("com.github.ElyteLabs:ElyteLabsAds:1.0.0")
}
```

---

## 🚀 Quick Start

### 1. Initialise

```kotlin
import com.elytelabs.ads.AdsManager
import com.elytelabs.ads.AdsConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Basic init (all defaults)
        AdsManager.init(this)

        // Or with custom config
        AdsManager.init(this, AdsConfig(
            cacheHours = 12,
            bannerRefreshSeconds = 45,
            maxAdsToFetch = 30
        ))
    }
}
```

### 2. Banner Ad

```xml
<com.elytelabs.ads.ui.BannerAdView
    android:id="@+id/fallbackBanner"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:visibility="gone" />
```

```kotlin
// In your AdMob failure callback:
override fun onAdFailedToLoad(error: LoadAdError) {
    AdsManager.addListener(object : AdsManager.AdLoadListener {
        override fun onAdsLoaded() {
            binding.fallbackBanner.loadAd()
        }
    })
}
```

> Auto-refreshes with a smooth crossfade. Interval is configurable.

### 3. Interstitial Ad

```kotlin
override fun onAdFailedToLoad(error: LoadAdError) {
    if (AdsManager.isInterstitialLoaded()) {
        InterstitialAdActivity.show(this)
    }
}
```

### 4. Native Ad (RecyclerView)

```xml
<com.elytelabs.ads.ui.NativeAdView
    android:id="@+id/nativeAd"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

```kotlin
// In your RecyclerView adapter:
override fun onBindViewHolder(holder: AdViewHolder, position: Int) {
    holder.nativeAdView.loadAd()
}
```

> Native ads do **not** auto-refresh — they load once and stay stable in the list.

---

## 📊 Event Tracking

Hook into ad lifecycle events for analytics:

```kotlin
import com.elytelabs.ads.AdListener

AdsManager.adListener = object : AdListener {
    override fun onAdImpression(ad: AdModel) {
        // Fired when banner loads, interstitial opens, or native ad binds
        analytics.logEvent("ad_impression", bundleOf("ad_id" to ad.id))
    }

    override fun onAdClicked(ad: AdModel) {
        // User tapped "Install"
        analytics.logEvent("ad_click", bundleOf("ad_id" to ad.id))
    }

    override fun onAdDismissed() {
        // Interstitial was closed
    }

    override fun onAdFailedToLoad(error: String) {
        // Network error or empty response
        Log.w("Ads", error)
    }
}
```

All methods have default no-op implementations — override only what you need.

---

## 🎨 Theming

Override SDK colors programmatically:

```kotlin
import com.elytelabs.ads.AdsTheme

AdsManager.theme = AdsTheme(
    buttonColor = Color.parseColor("#FF5722"),       // Install button background
    buttonTextColor = Color.WHITE,                    // Install button text
    badgeColor = Color.parseColor("#4CAF50"),          // "Ad" badge background
    bannerBackgroundColor = Color.parseColor("#FAFAFA") // Banner strip background
)
```

Leave any field `null` to keep the default (which auto-adapts to dark mode).

### Dark Mode

The SDK ships with `values-night/colors.xml` — dark mode works automatically. No code needed. If you set `AdsTheme` overrides, they take priority in both modes.

---

## ⚙️ Configuration Reference

```kotlin
data class AdsConfig(
    val cacheHours: Int = 24,          // Cache validity period
    val bannerRefreshSeconds: Int = 30, // Banner rotation interval
    val adType: String = "apps",        // "apps", "websites", or "all"
    val maxAdsToFetch: Int = 50         // Max items from API (max 50)
)
```

---

## 🏗️ Architecture

```
com.elytelabs.ads
├── AdsManager.kt        # Singleton: init, cache, randomisation, config
├── AdsConfig.kt         # Configuration data class
├── AdsTheme.kt          # Theming data class
├── AdListener.kt        # Event callback interface
├── models/
│   └── AdResponse.kt    # Gson data classes
├── network/
│   ├── AdsApi.kt        # Retrofit interface
│   └── AdsClient.kt     # Retrofit client
└── ui/
    ├── BannerAdView.kt          # Auto-refreshing banner with crossfade
    ├── NativeAdView.kt          # List-embeddable native ad
    └── InterstitialAdActivity.kt # Full-screen interstitial
```

---

## 🛡️ ProGuard / R8

The library ships `consumer-rules.pro` — **automatically applied**. No manual config needed.

---

## 📋 Google Play Policy Compliance

| Requirement | Implementation |
|-------------|---------------|
| **Ad labelling** | Yellow "Ad" badge on all formats. |
| **Dismissibility** | Interstitial has a prominent ✕ close button. |
| **No deceptive clicks** | Only "Install" button navigates. Background taps do nothing. |
| **No unexpected ads** | SDK only shows ads when explicitly called. |
| **User data** | No personal data collected. Only `packageName` for exclusion. |

---

## 📐 Requirements

| Requirement | Minimum |
|-------------|---------|
| Android SDK | API 25+ |
| Kotlin | 2.0+ |
| Gradle | 8.0+ |
| AGP | 8.0+ |

---

## 📄 License

```
MIT License
Copyright (c) 2026 Elyte Labs
```
