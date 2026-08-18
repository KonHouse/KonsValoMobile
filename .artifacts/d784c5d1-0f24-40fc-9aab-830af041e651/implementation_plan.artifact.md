# Implementation Plan - ValoMobile Updates

Implement UI and feature updates including removing the purchase button, adding a settings screen, and implementing "All Skins" and "Wishlist" tabs with adaptive support.

## User Review Required
> [!IMPORTANT]
> The "All Skins" catalog will fetch data from `valorant-api.com`. This data does not include prices by default (as it's a public static API). Skins from this catalog might show "0 VP" or I will handle them as "Price N/A" if not available in the local DB.

## Proposed Changes

### 1. UI Refinement
#### [MODIFY] [StoreDetailScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/store/StoreDetailScreen.kt)
- Remove the "Purchase Skin" button.

### 2. Navigation Update
#### [MODIFY] [ValoNavKey.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/navigation/ValoNavKey.kt)
- Add `Catalog`, `Wishlist`, and `Settings` keys.
#### [MODIFY] [ValoApp.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/ValoApp.kt)
- Add settings icon to `TopAppBar`.
- Add "Catalog" and "Wishlist" tabs to `NavigationBar`.
- Register new screens in `NavDisplay`.

### 3. Data Layer
#### [NEW] [ValorantApiService.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/remote/ValorantApiService.kt)
- Define interface for `valorant-api.com` endpoints (skins, content tiers).
#### [MODIFY] [NetworkModule.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/di/NetworkModule.kt)
- Provide `ValorantApiService`.
#### [NEW] [SkinCatalogRepository.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/repository/SkinCatalogRepository.kt)
- Fetch and map skins from the public API.
#### [MODIFY] [BackendAuthRepository.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/repository/BackendAuthRepository.java)
- Add `logout()` method (calls `clearConfig()`).

### 4. New Screens & ViewModels
#### [NEW] [SettingsViewModel.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/settings/SettingsViewModel.kt)
- Handle "Wishlist Notifications" (SharedPreferences) and logout.
#### [NEW] [SettingsScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/settings/SettingsScreen.kt)
- UI for settings.
#### [NEW] [CatalogViewModel.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/catalog/CatalogViewModel.kt)
- Fetch skins, search, sort, and handle wishlist toggling.
#### [NEW] [CatalogScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/catalog/CatalogScreen.kt)
- UI for "All Skins" tab.
#### [NEW] [WishlistScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/catalog/WishlistScreen.kt)
- UI for "Wishlist" tab.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure compilation.

### Manual Verification
- Verify "Buy" button is gone.
- Check "Settings" icon and screen.
- Verify "Logout" redirects to `ConnectScreen`.
- Verify "All Skins" fetches data and search/sort works.
- Verify Wishlist persistence and "Wishlist" tab.
