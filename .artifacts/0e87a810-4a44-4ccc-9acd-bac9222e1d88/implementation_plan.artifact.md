# ValoMobile UI Implementation Plan

Implement the Store Rotation, Featured Bundles, and Night Market screens with adaptive layouts, Navigation 3, and Wishlist functionality using mock data.

## Proposed Changes

### Domain Layer
Defines the core data structures.

#### [NEW] [SkinItem.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/domain/model/SkinItem.kt)
Data class for weapon skins, including price, tier, and image URL.
#### [NEW] [Bundle.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/domain/model/Bundle.kt)
Data class for featured bundles.

### Data Layer
Provides mock data for the UI.

#### [NEW] [MockStoreRepository.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/repository/MockStoreRepository.kt)
Repository that returns hardcoded lists of skins and bundles.

### UI Layer
Implements screens, components, and navigation.

#### [NEW] [ValoNavKey.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/navigation/ValoNavKey.kt)
`@Serializable` objects for Navigation 3 routes (StoreRotation, FeaturedBundles, NightMarket, StoreDetail).

#### [NEW] [StoreViewModel.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/store/StoreViewModel.kt)
Hilt ViewModel to manage store data and wishlist state.

#### [NEW] [SkinItemCard.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/components/SkinItemCard.kt)
Reusable component to display a skin item with a wishlist toggle.

#### [NEW] [StoreRotationScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/store/StoreRotationScreen.kt)
Daily offers view.

#### [NEW] [FeaturedBundlesScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/store/FeaturedBundlesScreen.kt)
Featured bundles view.

#### [NEW] [NightMarketScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/store/NightMarketScreen.kt)
Discounted skins view.

#### [NEW] [StoreDetailScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/store/StoreDetailScreen.kt)
Detail view for a specific skin item.

#### [NEW] [ValoApp.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/ValoApp.kt)
Main entry point for UI, setting up `NavDisplay` and `ListDetailPaneScaffold`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/MainActivity.kt)
Updates to call `ValoApp` and enable edge-to-edge.

## Verification Plan

### Automated Tests
- Build the project: `./gradlew :app:assembleDebug`

### Manual Verification
- Verify navigation between Store, Bundles, and Night Market via a Navigation Bar.
- Test wishlist toggle on various screens.
- Verify adaptive layout by rotating the device or using a tablet emulator (List and Detail shown side-by-side).
