# Update App Locale to English (en-US)

The goal is to switch the app's internal catalog and wishlist data fetching from Polish (`pl-PL`) to English (`en-US`). The private 'Store' data will remain untouched as per user instructions.

## Proposed Changes

### Data Layer

#### [MODIFY] [ValorantApiService.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/remote/ValorantApiService.kt)
- Update default `language` parameter in `getSkins` and `getContentTiers` to `"en-US"`.

#### [MODIFY] [SkinCatalogRepository.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/repository/SkinCatalogRepository.kt)
- Change hardcoded `"pl-PL"` to `"en-US"` in API calls.

## Verification Plan

### Automated Tests
- Run existing unit tests to ensure no regressions.
- Verify that `SkinCatalogRepository` now passes `"en-US"` to the API service (could be done via a mock test if exists, but I'll manually check the code).

### Manual Verification
- Verify that all skin names in the Catalog are in English.
- Verify that wishlist items (if they exist in catalog) are displayed in English.
