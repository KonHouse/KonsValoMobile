# Project Plan

Valorant store checking app. The app should allow users to check their in-game Valorant store, including daily offers and bundles, without having to launch the game.

## Project Brief

# Project Brief: ValoMobile (MVP)

ValoMobile is a lightweight Android application designed for Valorant players to track their in-game store rotation, featured bundles, and personalized Night Market offers without needing to launch the full game client.

## Features
1.  **Store & Bundle Viewer**: Securely retrieve and display the current 24-hour daily item offers and active featured weapon skin bundles.
2.  **Night Market Support**: Dedicated interface to view exclusive, personalized discounted offers during active Night Market events.
3.  **Skin Wishlist**: A personalized list where users can track specific weapon skins they are waiting to purchase.
4.  **Availability Notifications**: Automated alerts that notify the user immediately when a skin from their wishlist appears in their daily store rotation or Night Market.

## High-Level Technical Stack
*   **Primary Language**: **Java** (Core business logic and lifecycle management).
*   **UI Framework**: **Jetpack Compose** (Kotlin-based UI layer).
*   **Navigation**: **Jetpack Navigation 3** (State-driven navigation model).
*   **Adaptive Layouts**: **Compose Material Adaptive** (Ensuring a consistent experience across phones, foldables, and tablets).
*   **Concurrency**: **Kotlin Coroutines** (Essential for managing Compose state and asynchronous API calls).

> [!NOTE]
> While Java is the primary language for the application logic, Jetpack Compose and Navigation 3 are Kotlin-first libraries; therefore, the project will use a hybrid configuration to support modern UI components while maintaining Java for the broader architecture.

## Implementation Steps
**Total Duration:** 54m 55s

### Task_1_Infrastructure_API: Configure project for Java/Kotlin hybrid support, setup Hilt, and implement the Valorant API service layer for store and bundle data.
- **Status:** COMPLETED
- **Updates:** Configured Java/Kotlin hybrid project.
- **Acceptance Criteria:**
  - Project builds successfully
  - Valorant API client integrated with secure API_KEY management
  - Successful retrieval of store rotation and bundle data
- **Duration:** 9m 57s

### Task_2_Domain_Persistence: Implement Java-based business logic, skin data models, and Room database for managing the user's Skin Wishlist.
- **Status:** COMPLETED
- **Updates:** Implemented Java data models for Skins, Bundles, and Store items.
- **Acceptance Criteria:**
  - Skin and Store data models implemented in Java
  - Room database functional for Wishlist CRUD operations
- **Duration:** 7m 51s

### Task_3_Compose_UI_Adaptive: Develop the primary UI for Store Rotation, Featured Bundles, and Night Market using Jetpack Compose and Material Adaptive for multi-device support.
- **Status:** COMPLETED
- **Updates:** Implemented Store Rotation, Featured Bundles, and Night Market screens in Compose.
- **Acceptance Criteria:**
  - Adaptive layout works on phone and tablet/foldable
  - Store and Night Market data rendered correctly
  - Material 3 styling applied
- **Duration:** 14m 3s

### Task_4_Navigation_Notifications: Integrate Jetpack Navigation 3 for screen transitions and implement WorkManager for periodic store checks and wishlist notifications.
- **Status:** COMPLETED
- **Updates:** Verified HiltWorker configuration and updated worker scheduling policy.
- **Acceptance Criteria:**
  - Navigation 3 routing functional between Store, Wishlist, and Night Market
  - Background worker correctly triggers notifications when wishlist skins appear in store
- **Duration:** 6m 34s

### Task_5_Run_Verify: Perform a full application run to verify stability, feature completion, and UI fidelity.
- **Status:** COMPLETED
- **Updates:** Completed final verification.
- **Acceptance Criteria:**
  - Build pass
  - App does not crash during navigation or data refresh
  - All existing tests pass
  - Critic_agent verifies stability and alignment with requirements
- **Duration:** 8m 58s

### Task_6_Authentication_Security: Implement WebView-based Riot RSO login flow and secure token management using EncryptedSharedPreferences.
- **Status:** COMPLETED
- **Updates:** Implemented WebView-based Riot Sign-On (RSO).
- **Acceptance Criteria:**
  - WebView login captures auth tokens (Access Token, Entitlements)
  - Tokens stored securely in EncryptedSharedPreferences
  - Riot session state correctly managed via RiotAuthRepository
- **Duration:** 4m 21s

### Task_7_Data_Integration_Verification: Transition to RiotStoreRepository for real-time store data, integrate dynamic skin catalog, and perform final verification.
- **Status:** COMPLETED
- **Updates:** Implemented RiotStoreRepository to fetch real-time store data from Riot servers.
Integrated Retrofit services for Storefront, Offers, and Night Market endpoints.
Implemented dynamic skin catalog mapping using valorant-api.com.
Updated ViewModel and UI to handle real data streams, loading, and auth errors.
Removed all mock data from the primary application flow.
- **Acceptance Criteria:**
  - Real user store/bundle data displayed in UI replacing mock data
  - Dynamic skin catalog integrated with latest asset data from Valorant API
  - Build pass
  - App does not crash
  - All existing tests pass
  - Critic_agent verifies stability and alignment with requirements
- **Duration:** 3m 11s

