# ValoMobile UI Implementation Plan

This plan outlines the steps to implement the primary UI for ValoMobile, including the Store Rotation, Featured Bundles, and Night Market screens, using Jetpack Compose, Material 3, and Navigation 3.

## User Review Required

> [!IMPORTANT]
> The HenrikDev API (v2) does not natively support individual user Store Rotation or Night Market data without private Riot credentials. I will implement a robust UI that renders this data, initially using a combination of the global "Offers" API and mocked data to demonstrate the functionality.

## Proposed Changes

### Domain Layer

#### [NEW] [NightMarketOffer.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/domain/model/NightMarketOffer.java)
- Represents a discounted weapon skin in the Night Market.

#### [NEW] [StoreRotation.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/domain/model/StoreRotation.java)
- Container for daily weapon offers.

---

### Data Layer

#### [MODIFY] [ValorantApiService.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/api/ValorantApiService.java)
- Add `/valorant/v2/store-offers` to fetch all available skins and their prices.

#### [MODIFY] [StoreRepository.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/repository/StoreRepository.java)
- Add methods to fetch store rotation and night market data (with mocking for missing API features).

---

### UI Layer

#### [MODIFY] [StoreState.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/StoreState.java)
- Update to include `dailyOffers`, `featuredMenu`, and `nightMarket` data.

#### [MODIFY] [StoreViewModel.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/StoreViewModel.java)
- Update to fetch and manage the new store data categories.

#### [NEW] [MainScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/MainScreen.kt)
- Root UI component using Navigation 3 and `NavDisplay`.
- Implements a Bottom Navigation Bar or Navigation Rail based on screen size.

#### [NEW] [StoreRotationScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/StoreRotationScreen.kt)
- Displays daily weapon offers.
- Uses `ListDetailPaneScaffold` for adaptive layout.

#### [NEW] [FeaturedMenuScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/FeaturedMenuScreen.kt)
- Displays featured bundles.
- Uses `ListDetailPaneScaffold`.

#### [NEW] [NightMarketScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/NightMarketScreen.kt)
- Displays discounted Night Market offers with card-flip animations or expressive Material 3 cards.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/MainActivity.kt)
- Wire up `MainScreen` as the content.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:testDebugUnitTest` to verify Repository and ViewModel logic.

### Manual Verification
- Deploy to a phone emulator and verify the bottom navigation and screen transitions.
- Deploy to a tablet/foldable emulator and verify the `ListDetailPaneScaffold` adaptive behavior.
- Verify that Material 3 styling (Dynamic Color) is applied.
