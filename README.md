# Elyte Labs Ads SDK

[![](https://jitpack.io/v/ElyteLabs/ElyteLabsAds.svg)](https://jitpack.io/#ElyteLabs/ElyteLabsAds)
[![API](https://img.shields.io/badge/API-25%2B-brightgreen.svg)](https://android-arsenal.com/api?level=25)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight, zero-dependency fallback ad network SDK for Android. Designed to display cross-promotional house ads for Elyte Labs applications **only when primary ad providers (AdMob, etc.) fail to fill**.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🎯 **Fallback-First Design** | Built to activate only on `onAdFailedToLoad` — never competes with paid ads. |
| ⚡ **24-Hour Offline Cache** | Fetches once, serves instantly from `SharedPreferences` for 24 hours. Works offline. |
| 🔀 **Auto-Randomisation** | Every ad impression pulls a different app from the 50-item pool. |
| 🚫 **Self-Exclusion** | Automatically excludes the host app's package from the ad pool. |
| 🖼️ **Glide Preloading** | Icons are pre-fetched into memory on init — zero visible latency. |
| 🔄 **Banner Auto-Refresh** | Banner rotates to a new ad every 30 seconds automatically. |
| 📱 **Edge-to-Edge** | Full `WindowInsets` support for Android 15 (API 35+) gesture navigation. |
| ♿ **Accessible** | `contentDescription` on every interactive element. |
| 🛡️ **Policy Compliant** | Visible close button, no deceptive click areas, clear "Ad" badge labelling. |

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

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.ElyteLabs:ElyteLabsAds:1.0.0")
}
```

---

## 🚀 Quick Start

### 1. Initialise the SDK

Call `AdsManager.init()` early in your app lifecycle (e.g. `Application.onCreate()` or `MainActivity.onCreate()`):

```kotlin
import com.elytelabs.ads.AdsManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AdsManager.init(this)
    }
}
```

### 2. Display a Banner Ad

Add the view to your XML layout:

```xml
<com.elytelabs.ads.ui.BannerAdView
    android:id="@+id/fallbackBanner"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:visibility="gone" />
```

Load it when your primary ad fails:

```kotlin
// Inside your AdMob banner listener:
override fun onAdFailedToLoad(error: LoadAdError) {
    AdsManager.addListener(object : AdsManager.AdLoadListener {
        override fun onAdsLoaded() {
            binding.fallbackBanner.loadAd()
        }
    })
}
```

> The banner auto-refreshes every 30 seconds. No additional code needed.

### 3. Display an Interstitial Ad

```kotlin
// Inside your AdMob interstitial failure callback:
override fun onAdFailedToLoad(error: LoadAdError) {
    if (AdsManager.isInterstitialLoaded()) {
        InterstitialAdActivity.show(this@MainActivity)
    }
}
```

---

## 🏗️ Architecture

```
com.elytelabs.ads
├── AdsManager.kt              # Singleton: init, cache, randomisation
├── models/
│   └── AdResponse.kt          # Gson data classes (AdResponse, AdModel)
├── network/
│   ├── AdsApi.kt              # Retrofit interface
│   └── AdsClient.kt           # Retrofit singleton client
└── ui/
    ├── BannerAdView.kt         # Custom FrameLayout banner with auto-refresh
    └── InterstitialAdActivity.kt  # Full-screen ad Activity
```

### Data Flow

```
AdsManager.init(context)
    │
    ├─ Cache valid? ──▶ Load from SharedPreferences ──▶ Notify listeners
    │
    └─ Cache expired? ──▶ GET /api/promote?limit=50&type=apps&exclude={pkg}
                              │
                              ├─ Save to SharedPreferences
                              ├─ Preload icons via Glide
                              └─ Notify listeners
```

---

## 🔧 API Parameters

The SDK hits `GET https://elytelabs.com/api/promote` with:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `limit` | `Int` | `50` | Maximum items to return (max 50). |
| `type` | `String` | `"apps"` | Filter: `"apps"`, `"websites"`, or `"all"`. |
| `exclude` | `String` | Host app package | Package ID to exclude from results. |

The response is edge-cached for 1 hour server-side and 24 hours client-side.

---

## 🛡️ ProGuard / R8

The library ships a `consumer-rules.pro` that is **automatically applied** when your app enables R8. No manual configuration needed.

If you need to inspect the rules:

```proguard
# Shipped inside the AAR:
-keep class com.elytelabs.ads.models.** { *; }
-keep class com.elytelabs.ads.AdsManager { *; }
-keep class com.elytelabs.ads.ui.BannerAdView { *; }
-keep class com.elytelabs.ads.ui.InterstitialAdActivity { *; }
```

---

## 📋 Google Play Policy Compliance

This SDK is designed to comply with Google Play's [Ads policy](https://support.google.com/googleplay/android-developer/answer/9857753):

| Requirement | Implementation |
|-------------|---------------|
| **Ad labelling** | Yellow "Ad" badge is always visible on both banner and interstitial. |
| **Dismissibility** | Interstitial has a prominent ✕ close button in the top-right corner. |
| **No deceptive clicks** | Only the "Install" button navigates to the Play Store. Tapping outside does nothing. |
| **No unexpected ads** | SDK only shows ads when explicitly called by the developer. |
| **User data** | No personal user data is collected. Only `context.packageName` is sent for exclusion filtering. |

---

## 🔄 Lifecycle Best Practices

```kotlin
class MyActivity : AppCompatActivity() {

    private val adListener = object : AdsManager.AdLoadListener {
        override fun onAdsLoaded() {
            binding.fallbackBanner.loadAd()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdsManager.init(this)
        AdsManager.addListener(adListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        AdsManager.removeListener(adListener)  // Prevent leaks
    }
}
```

> **Note:** Listeners are held via `WeakReference` internally as a safety net, but explicit removal in `onDestroy()` is still recommended best practice.

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

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
