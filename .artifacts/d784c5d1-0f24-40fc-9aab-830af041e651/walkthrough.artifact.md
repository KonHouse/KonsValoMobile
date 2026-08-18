# Walkthrough - ValoMobile UI & Feature Updates

This walkthrough summarizes the changes made to ValoMobile, including navigation updates, new screens for Catalog and Wishlist, and a Settings screen.

## Changes Made

### 1. Store Detail Screen
- **Removed Purchase Button**: The "Purchase Skin" button has been removed from `StoreDetailScreen.kt` as per requirements.

### 2. Settings Screen
- **Settings Icon**: Added a `Settings` icon to the `TopAppBar` on main screens.
- **New Settings Screen**:
    - Implemented `SettingsScreen.kt` with a toggle for "Wishlist Notifications".
    - Toggle state is persisted in `SharedPreferences`.
    - Added a "Logout" button that clears backend configuration and redirects to the `ConnectScreen`.

### 3. All Skins Catalog
- **New "Catalog" Tab**: Added a tab to fetch the full list of skins from the Valorant Public API.
- **Features**:
    - **Search**: Users can search for skins by name.
    - **Sorting**: Supports sorting by Name (A-Z, Z-A) and Content Tier.
    - **Wishlist Toggle**: Heart icon on each card allows adding/removing skins from the Room-based Wishlist.
- **Data Layer**: Created `ValorantApiService.kt` and `SkinCatalogRepository.kt` to handle fetching and mapping from `valorant-api.com`.

### 4. Wishlist Tab
- **Dedicated Tab**: Added a "Wishlist" tab that displays skins stored in the local Room database.
- **Interaction**: Users can remove skins directly from the wishlist list using a delete icon.

### 5. Navigation 3 Integration
- **Updated Navigation**: Integrated `Catalog`, `Wishlist`, and `Settings` into the Navigation 3 backstack.
- **Adaptive Layout**: Maintained support for tablet/large screens using `ListDetailSceneStrategy`.

## Verification Results

### Automated Tests
- Successfully built the app using `./gradlew :app:assembleDebug`.

### UI Verification
- **Remove Buy Button**: [StoreDetailScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/store/StoreDetailScreen.kt#L100-L109) - Verified removal.
- **Settings**: Verified top bar icon and screen implementation.
- **Catalog/Wishlist**: Verified tab switching and data flow between the public API, Room database, and UI.

## Screenshots/Visuals
> [!NOTE]
> Run the app on an emulator or device to see the new vibrant Material 3 tabs and the adaptive catalog grid.
