# ValoMobile UI Walkthrough

The ValoMobile UI has been implemented using Jetpack Compose, Material 3, and Navigation 3, with full support for adaptive layouts.

## Key Features

### 1. Store Rotation & Night Market
- Displays a grid of skin items with prices and weapon types.
- Night Market items show discount percentages and original prices.
- Each item has a wishlist heart icon that toggles its state.

### 2. Featured Bundles
- Displays large cards for bundles with cover images and descriptions.
- Lists all items included in the bundle with a "View" button for details.

### 3. Adaptive Layout (List-Detail)
- On phones, the app navigates between list screens and a full-screen detail view.
- On tablets/large screens, the app shows the list and the selected item's details side-by-side using `ListDetailPaneScaffold` (integrated via `ListDetailSceneStrategy`).

### 4. Navigation 3
- Seamless transitions between Store, Bundles, and Night Market via a bottom navigation bar.
- State-driven navigation using `@Serializable` keys.

### 5. Wishlist
- Global wishlist state managed in `StoreViewModel`.
- Changes to the wishlist are reflected across all screens instantly.

## Screenshots/Previews
- `StoreRotationScreenPreview`: Mobile view of the store.
- `ValoAppTabletPreview`: Landscape tablet view showing list and detail panes.

## Technical Details
- **Architecture**: MVVM with Hilt for dependency injection.
- **UI Toolkit**: Jetpack Compose with Material 3.
- **Navigation**: Jetpack Navigation 3 (Alpha/RC versions for latest features).
- **Adaptive**: Material 3 Adaptive library for canonical layouts.
- **Image Loading**: Coil for asynchronous image loading from Valorant API URLs.
