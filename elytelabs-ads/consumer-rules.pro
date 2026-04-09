# Consumer ProGuard rules for elytelabs-ads
# These rules are automatically applied to any app that depends on this library.

# Keep Gson-serialized model classes (reflection-based deserialization)
-keep class com.elytelabs.ads.models.** { *; }
-keepclassmembers class com.elytelabs.ads.models.** { *; }

# Keep public API surface
-keep class com.elytelabs.ads.AdsManager { *; }
-keep class com.elytelabs.ads.AdsManager$AdLoadListener { *; }
-keep class com.elytelabs.ads.AdsConfig { *; }
-keep class com.elytelabs.ads.AdsTheme { *; }
-keep class com.elytelabs.ads.AdListener { *; }
-keep class com.elytelabs.ads.ui.BannerAdView { *; }
-keep class com.elytelabs.ads.ui.NativeAdView { *; }
-keep class com.elytelabs.ads.ui.NativeAdMediumView { *; }
-keep class com.elytelabs.ads.ui.InterstitialAdActivity { *; }

# Retrofit interface
-keep,allowobfuscation interface com.elytelabs.ads.network.AdsApi

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
