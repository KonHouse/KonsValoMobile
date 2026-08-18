# Login System Rebuild Plan

Rebuilding the login system to improve stability, provide multiple login methods, and enhance the user experience with a robust fallback system.

## Proposed Changes

### Data Layer

#### [MODIFY] [RiotAuthRepository.java](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/data/repository/RiotAuthRepository.java)
- Simplify token management.
- Add/Rename methods: `isLoggedIn()`, `logout()`, `saveTokens()`, `saveEntitlementsToken()`.
- Improve `EncryptedSharedPreferences` initialization with better error handling and logging.

### UI Layer

#### [MODIFY] [LoginViewModel.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/login/LoginViewModel.kt)
- Update `LoginState` sealed class: `Initial`, `Loading`, `Success`, `Error`.
- Define "Golden" Login Methods: `MOBILE_CLIENT`, `PC_CLIENT`, `RIOT_MOBILE`.
- Implement robust `extractTokens(url)` supporting both fragments and queries.
- Add `logout()` to clear repository tokens.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/login/LoginScreen.kt)
- Modernize UI using Material 3 `Scaffold` and `TopAppBar`.
- Implement `LoginMethodSelector` using `PrimaryTabRow` or `SingleChoiceSegmentedButtonRow`.
- Enhance `WebView` integration:
    - Stealth User-Agent.
    - Overlay Loading indicator.
    - "Open in External Browser" and "Manual URL Paste" accessible from the main screen.
- Ensure cookies/cache are cleared on each login attempt.

## Verification Plan

### Automated Tests
- Unit test for `LoginViewModel#extractTokens` with various URL formats (fragment vs query).

### Manual Verification
- Verify that switching between login methods reloads the WebView with correct parameters.
- Verify that "Open in External Browser" launches the system browser.
- Verify that manual URL pasting works for all three methods.
- Verify successful transition to `StoreScreen` after login.
