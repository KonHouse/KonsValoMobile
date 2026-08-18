# Fix WorkManager Hilt Injection and Image Loading

This plan addresses the `NoSuchMethodException` in `WishlistNotificationWorker` and the broken images in the UI.

## Proposed Changes

### WorkManager & Hilt Injection

#### [MODIFY] [ValoApplication.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ValoApplication.kt)
- Change `workManagerConfiguration` from a property override to an explicit `getWorkManagerConfiguration()` function override to ensure compatibility with the Java interface.
- Add logging to verify `workerFactory` injection.

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/AndroidManifest.xml)
- Ensure the WorkManager initializer is correctly disabled. I will use a more explicit removal if needed.

### Image Loading

#### [MODIFY] [MockStoreRepository.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/repository/MockStoreRepository.kt)
- Update mock data with correct UUIDs and verified `displayIcon` URLs from `valorant-api.com`.
- Fix the issue where multiple skins shared the same (possibly invalid) UUID.

## Verification Plan

### Automated Tests
- I will attempt to build the project using `./gradlew :app:assembleDebug`.

### Manual Verification
- Verify that `ValoApplication` successfully injects `HiltWorkerFactory`.
- Check if images load correctly in the `SkinItemCard` (via manual check of URLs if possible, or by ensuring the URLs are valid).
