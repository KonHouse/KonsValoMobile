# Transition ValoMobile to Real-Time Store Data

This plan outlines the steps to replace `MockStoreRepository` with `RiotStoreRepository` using real-time data from Riot's servers.

## User Review Required

> [!IMPORTANT]
> The app requires the user to be logged in to fetch store data. The implementation assumes `access_token` and `entitlements_token` are available in `RiotAuthRepository`.

## Proposed Changes

### Data Layer

#### [NEW] [RiotApiService.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/remote/RiotApiService.java)
Retrofit interface for Riot Store APIs.
- `getStorefront(region, puuid)`
- `getOffers(region)`
- `getNightMarket(region)` (if different from storefront)

#### [NEW] [ValorantApiComService.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/remote/ValorantApiComService.java)
Retrofit interface for `valorant-api.com`.
- `getSkins()`

#### [NEW] [Remote Models](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/remote/model/)
Java data classes for API responses.

#### [NEW] [RiotStoreRepository.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/repository/RiotStoreRepository.java)
Java implementation of the store repository.
- Fetches daily offers, bundles, and night market.
- Uses `valorant-api.com` to map UUIDs to names and icons.
- Handles region detection (defaulting to 'na').

#### [MODIFY] [NetworkModule.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/di/NetworkModule.kt)
Add provides for `RiotApiService` and `ValorantApiComService`.

### UI Layer

#### [MODIFY] [StoreViewModel.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/store/StoreViewModel.kt)
- Replace `MockStoreRepository` with `RiotStoreRepository`.
- Update `loadData` to fetch real data.
- Handle potential errors (e.g., token expiration).

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- Run `StoreUpdateWorkerTest.kt` (if applicable) or create a new unit test for `RiotStoreRepository`.

### Manual Verification
- Launch the app and navigate to the Store screen.
- Verify that real skin data is displayed (images, names, prices).
- Verify that bundles are correctly shown.
- Check logs for any API errors or token issues.
