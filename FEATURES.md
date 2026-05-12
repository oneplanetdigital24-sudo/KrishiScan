# KrishiScan Features

This document explains the working features, app pages, backend routes, and expected user flows for the KrishiScan Android application.

## Application Overview

KrishiScan is an Android crop disease detection app for farmers. It supports Firebase authentication, image-based crop disease scanning using an on-device TensorFlow Lite model, backend image upload, scan history, AI treatment suggestions, chatbot support, and profile management.

The project has two main parts:

- Android app: `mobile-android/app`
- Backend API: `backend-api`

The Android app communicates with the backend through:

```text
https://krishiscan-backend-api.onrender.com/api/v1/
```

For all backend features to work, the deployed backend must contain the latest code changes.

## Pages And Features

## 1. Splash Screen

File:

```text
mobile-android/app/src/main/java/com/krishiscan/app/activities/SplashActivity.java
```

Purpose:

- Shows the app launch screen briefly.
- Checks Firebase login state.
- Sends logged-in users to the main app.
- Sends logged-out users to the authentication page.

Working behavior:

- If `FirebaseAuth.getInstance().getCurrentUser()` is `null`, the app opens `AuthActivity`.
- If a Firebase user exists, the app opens `MainActivity`.
- Splash delay is short and simple.

## 2. Registration And Login Page

File:

```text
mobile-android/app/src/main/java/com/krishiscan/app/activities/AuthActivity.java
```

Purpose:

- Allows users to register, log in, reset password, or use Google Sign-In.

Working features:

- Email/password registration.
- Email/password login.
- Google Sign-In.
- Password reset email.
- Basic input validation before Firebase is called.
- Backend session creation after successful Firebase login/register.

Validation:

- Email is required.
- Email must be valid.
- Password is required.
- Password must be at least 6 characters.

Backend route used:

```text
POST /auth/session
```

Expected registration flow:

1. User enters email and password.
2. Android creates the Firebase Auth account.
3. Android requests a Firebase ID token.
4. Android calls backend `POST /auth/session`.
5. Backend creates or loads the Firestore user profile.
6. App opens the main screen.

Important backend behavior:

- If the user profile does not exist, the backend creates a minimal user document with name, email, language, role, created date, and updated date.

## 3. Main Screen And Bottom Navigation

File:

```text
mobile-android/app/src/main/java/com/krishiscan/app/activities/MainActivity.java
```

Purpose:

- Hosts the main app pages.
- Provides bottom navigation.

Pages:

- Scan
- Chat
- Profile

Working behavior:

- App opens the Scan page by default.
- Bottom navigation switches between Scan, Chat, and Profile.

## 4. Scan Page

File:

```text
mobile-android/app/src/main/java/com/krishiscan/app/fragments/ScanFragment.java
```

Purpose:

- Lets the user select or capture a crop image.
- Runs disease detection using the local TFLite model.
- Uploads the selected image.
- Saves the scan result.
- Shows recent scan history.

Working features:

- Pick image from gallery.
- Capture image using camera.
- Preview selected image.
- Compress selected/captured image before upload.
- Run on-device disease classification.
- Upload scan image to backend.
- Save scan result to backend.
- Open result page after successful scan.
- Load recent scan history.
- Open previous scan result from history.

Model files:

```text
mobile-android/app/src/main/assets/krishiscan_model.tflite
mobile-android/app/src/main/assets/labels.txt
```

Backend routes used:

```text
POST /uploads/scan-image
POST /scans
GET /scans
```

Expected scan flow:

1. User chooses Gallery or Camera.
2. App loads and previews image.
3. App compresses image to avoid upload size failure.
4. User taps Analyze.
5. TFLite model predicts crop and disease.
6. App uploads image to backend.
7. Backend stores image in Firebase Storage.
8. App sends prediction details to backend.
9. Backend saves the scan in Firestore.
10. Backend generates treatment text using AI, or returns fallback treatment if AI is unavailable.
11. App opens the Result page.
12. Scan history refreshes.

Failure handling:

- If no image is selected, app asks user to select or capture an image.
- If image cannot be decoded, app shows invalid image message.
- If model is missing, app shows model initialization error.
- If AI treatment fails, backend still saves scan and returns fallback treatment.

## 5. Result Page

File:

```text
mobile-android/app/src/main/java/com/krishiscan/app/fragments/ResultFragment.java
```

Purpose:

- Displays the result of a scan.

Working features:

- Shows crop name.
- Shows disease name.
- Shows confidence.
- Shows severity.
- Shows treatment suggestion.
- Allows sharing the result text.
- Shows saved history status.

Data shown:

- Scan ID
- Crop name
- Disease name
- Confidence score
- Severity
- Treatment

## 6. Scan History

Files:

```text
mobile-android/app/src/main/java/com/krishiscan/app/adapters/ScanHistoryAdapter.java
mobile-android/app/src/main/java/com/krishiscan/app/fragments/ScanFragment.java
mobile-android/app/src/main/java/com/krishiscan/app/fragments/ProfileFragment.java
```

Purpose:

- Shows saved scan records.

Working features:

- Scan page shows recent scans.
- Profile page shows scan history.
- Tapping a history item opens the Result page.
- History refreshes after a new scan is saved.
- Backend timestamp values are serialized safely for Android.

Backend route used:

```text
GET /scans
```

Stored scan fields:

- `scanId`
- `userId`
- `cropName`
- `diseaseName`
- `confidence`
- `severity`
- `imageUrl`
- `imagePath`
- `treatment`
- `createdAt`

## 7. Chatbot Page

File:

```text
mobile-android/app/src/main/java/com/krishiscan/app/fragments/ChatFragment.java
```

Purpose:

- Provides farming help through KrishiBot.

Working features:

- Loads previous chat history.
- Sends user messages.
- Shows local user message immediately.
- Receives AI reply from backend.
- Shows quick prompt chips.
- Stores chat messages in Firestore.
- Returns fallback reply if AI service is unavailable.

Backend routes used:

```text
POST /chat/messages
GET /chat/messages
```

Quick prompts:

- My tomato leaves are turning yellow
- Best time to plant rice in Assam?
- Government schemes for farmers
- How to make organic pesticide?
- Soil testing near me

Expected chat flow:

1. User opens Chat page.
2. App loads saved chat history.
3. User types message or taps quick prompt.
4. App immediately displays user message.
5. Backend saves user message.
6. Backend asks Gemini for reply.
7. If Gemini works, AI reply is returned.
8. If Gemini fails, fallback farming guidance is returned.
9. Backend saves AI message.
10. App displays reply.

## 8. Profile Page

File:

```text
mobile-android/app/src/main/java/com/krishiscan/app/fragments/ProfileFragment.java
```

Purpose:

- Displays and updates user profile.
- Shows user scan history.
- Allows logout.

Working features:

- Loads signed-in user profile.
- Shows name, state, and role.
- Lets user update name, phone, and state.
- Validates profile fields before backend update.
- Shows scan history.
- Opens history result details.
- Logs out from Firebase.

Backend routes used:

```text
GET /users/me
PATCH /users/me
GET /scans
```

Profile validation:

- Name is optional when unchanged, but if entered it must be at least 3 characters.
- Phone is optional, but if entered it must be a valid 10 digit Indian mobile number starting with 6, 7, 8, or 9.
- State is optional, but if entered it must be at least 2 characters.
- Empty profile update is not submitted.

Expected profile update flow:

1. User opens Profile page.
2. App loads profile using backend.
3. User edits name, phone, or state.
4. App validates fields.
5. App calls backend `PATCH /users/me`.
6. Backend updates Firestore.
7. App shows updated profile and success message.

## 9. Logout

Location:

```text
mobile-android/app/src/main/java/com/krishiscan/app/fragments/ProfileFragment.java
```

Working behavior:

- User taps logout.
- Firebase signs out the current user.
- App opens the authentication page.
- Current main activity is closed.

## Backend Features

## Authentication

Routes:

```text
POST /api/v1/auth/session
```

Features:

- Verifies Firebase ID token.
- Creates user profile if missing.
- Returns user data to Android.

## Users

Routes:

```text
GET /api/v1/users/me
PATCH /api/v1/users/me
```

Features:

- Loads current user profile.
- Updates allowed profile fields.
- Serializes Firestore timestamps safely.

## Uploads

Routes:

```text
POST /api/v1/uploads/scan-image
```

Features:

- Receives multipart image upload.
- Stores image in Firebase Storage.
- Returns image URL and storage path.

## Scans

Routes:

```text
POST /api/v1/scans
GET /api/v1/scans
GET /api/v1/scans/:scanId
DELETE /api/v1/scans/:scanId
```

Features:

- Saves scan prediction.
- Generates severity from confidence.
- Generates AI treatment when Gemini is available.
- Returns fallback treatment when Gemini is unavailable.
- Lists scan history.
- Gets one scan.
- Deletes scan and storage image.

## Chat

Routes:

```text
POST /api/v1/chat/messages
GET /api/v1/chat/messages
```

Features:

- Saves user messages.
- Generates AI reply when Gemini is available.
- Saves AI replies.
- Lists chat history.
- Returns fallback reply when Gemini is unavailable.

## Important Dependencies

Firebase:

- Firebase Authentication is required for registration/login.
- Firestore is required for user profiles, scans, and chat history.
- Firebase Storage is required for scan image uploads.

Gemini:

- Used for chatbot replies.
- Used for treatment generation.
- App still works with fallback text if Gemini fails.

Redis:

- Used for rate limiting.
- Backend now skips rate limiting safely if Redis is unavailable.

TensorFlow Lite:

- Used locally on Android for image disease detection.
- Requires model and labels in Android assets.

## Complete Demo Flow

1. Open app.
2. Splash screen appears briefly.
3. Register with email and password.
4. App opens main Scan page.
5. Choose image from Gallery or Camera.
6. Tap Analyze.
7. App detects crop disease.
8. App uploads image and saves result.
9. Result page shows crop, disease, confidence, severity, and treatment.
10. Go back to Scan or open Profile.
11. Scan history appears.
12. Open Chat page.
13. Send a farming question.
14. Chatbot replies.
15. Open Profile page.
16. Update profile details.
17. Logout.

## Common Issues To Check

If registration fails:

- Confirm Firebase Email/Password provider is enabled.
- Confirm Android `google-services.json` belongs to the same Firebase project as backend credentials.
- Confirm deployed backend is updated.

If scan upload fails:

- Confirm Firebase Storage bucket is configured.
- Confirm backend has Firebase Admin access.
- Confirm Storage bucket name is correct in backend environment variables.

If chatbot fails:

- Confirm backend is deployed.
- Confirm `GEMINI_API_KEY` exists.
- If Gemini is down or quota-limited, fallback reply should still appear.

If history does not show:

- Confirm backend is deployed with timestamp serialization fix.
- Confirm user is logged in.
- Confirm scans or chat messages exist for that Firebase user.

If profile update fails:

- Confirm name, phone, and state pass validation.
- Confirm backend is deployed.
- Confirm Firestore user document exists.

## Verification Commands

Backend typecheck:

```powershell
cd backend-api
npm run typecheck
```

Backend build:

```powershell
cd backend-api
npm run build
```

Android debug APK build:

```powershell
cd mobile-android
.\gradlew.bat assembleDebug
```

Debug APK output:

```text
mobile-android/app/build/outputs/apk/debug/app-debug.apk
```

