# Implementation Plan - Riot Login Fallback

The Riot login in the WebView is sometimes blocked by anti-bot measures. This plan implements a fallback mechanism using an external browser and manual URL entry.

## User Review Required

> [!IMPORTANT]
> The user will need to manually copy the URL from their external browser after a "failed" redirect (to playvalorant.com) and paste it into the app. This is a common workaround for Riot login issues in third-party apps.

## Proposed Changes

### [Login Component]

#### [MODIFY] [LoginViewModel.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/login/LoginViewModel.kt)
- Add `onManualUrlEntered(url: String)` to parse the redirect URL and extract tokens.
- Move `extractSubFromIdToken` logic to the ViewModel or a utility if needed, but keeping it in the extraction flow is fine.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/konra/AndroidStudioProjects/ValoMobile/app/src/main/java/com/example/valomobile/ui/screens/login/LoginScreen.kt)
- Add "Refresh" and "Clear Cookies" actions to the `TopAppBar`.
- Add a "Problems logging in?" button at the bottom of the screen or in a floating container.
- Implement a `ManualLoginDialog` with:
    - "Open in External Browser" button.
    - `TextField` for pasting the URL.
    - "Log In" button to process the pasted URL.
- Use `LocalContext.current.startActivity` to open the external browser.

## Verification Plan

### Automated Tests
- N/A (UI focused change, manual verification is more effective for WebView/External browser interaction).

### Manual Verification
1. Open the Login screen.
2. Click "Problems logging in?".
3. Click "Open in External Browser".
4. Log in in the external browser.
5. Copy the URL from the browser (e.g., `https://playvalorant.com/opt_in#access_token=...`).
6. Paste the URL into the app and click "Log In".
7. Verify the user is logged in successfully.
8. Test "Refresh" and "Clear Cookies" buttons.
