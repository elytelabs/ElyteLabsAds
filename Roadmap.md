# Elyte Labs Ads - SDK Roadmap

## Completed Features ✓
- [x] Modernize architecture to pure multi-module `elytelabs-ads` library wrapper.
- [x] Standardize UI configurations bridging API 26 support to API 36 Edge-to-Edge logic.
- [x] Design premium UI aesthetics mimicking massive traditional Ad Networks (Full screen blur).
- [x] Implement core Query Parameters ensuring dynamic API payloads.
- [x] Prevent recursive promotion (safely pass `exclude=packageName` API constraint).
- [x] Establish a 24-Hour Zero-Latency Cache via local `SharedPreferences`.
- [x] Execute automatic Ad array randomization to eliminate ad-fatigue. 

## Active Requirements ✓
- [x] **Device Verification:** Deep verify layouts / dependencies locally on diverse physical emulators prior to release.
- [x] **Fall-Back Ecosystem Integration:** Draft explicit README instructions on strictly invoking `AdsManager` exclusively on `onAdFailedToLoad` callbacks from primary systems (like AdMob) to optimize revenue hierarchy.
- [x] **SDK Documentation:** Generate a proper `README.md` containing initialization code blocks, lifecycle patterns, and SDK restrictions. 

## Future Expansion Opportunities
- [ ] **Native Ad Formats:** Construct a customizable `ElyteLabsNativeAdView` so developer partners can integrate ads flawlessly indoors, like within `RecyclerViews` seamlessly matching the app design!
- [ ] **True Autonomous Lifecycle:** Inject `androidx.lifecycle:lifecycle-process` so developers never have to manually call `AdsManager` inside their `Activities` again. The library will track background/foreground transitions independently!
- [ ] **Analytics Engine:** Introduce a lightweight POST subsystem that pings the Elyte Labs dashboard whenever a user looks at an ad (Impression) versus clicks it (Click_Through).
- [ ] **Jitpack Release Configuration:** Build out the publishing workflow configurations to automate pushing code straight to JitPack via GitHub triggers.