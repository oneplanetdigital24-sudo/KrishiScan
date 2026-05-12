# KrishiScan Android App

Native Android client for KrishiScan with Firebase Authentication, on-device TFLite crop disease detection, backend image upload, scan history, treatment generation, KrishiBot chat, profile, and APK build steps.

## What Is Working

- Splash screen routes signed-in users to the app and unsigned users to auth.
- Auth screen supports email/password login, registration, password reset, and Google Sign-In.
- Scan screen supports camera capture, gallery selection, TFLite prediction, backend upload, scan save, and recent history.
- Result screen shows crop, disease, confidence, severity, treatment, saved-history status, and native share.
- Chat screen loads history, sends messages to the backend, and shows AI replies.
- Profile screen loads the signed-in user profile, lets the user edit name, phone, and state, shows scan history, opens details, and logs out.

## Project Paths

- Android app: `mobile-android/app`
- Android package name: `com.krishiscan.app`
- Backend API: `../backend-api`
- Firebase rules: `../infra/firebase`
- Main Android README: `mobile-android/README.md`
- Training README: `mobile-android/README_COLAB_TRAINING_A_TO_Z.md`

## Required Tools

- Android Studio latest stable
- JDK 17
- Android SDK 35 installed from Android Studio SDK Manager
- Node.js 18 or newer
- npm
- Firebase CLI for deploying rules
- Redis for local backend rate limiting
- Firebase project
- Gemini API key

## 1. Create And Configure Firebase

1. Open Firebase Console.
2. Create a project, for example `krishiscan`.
3. Go to **Build > Authentication > Sign-in method**.
4. Enable **Email/Password**.
5. Enable **Google**.
6. For Google Sign-In, add your app signing certificate fingerprints:

```powershell
cd mobile-android
keytool -list -v -alias androiddebugkey -keystore "$env:USERPROFILE\.android\debug.keystore" -storepass android -keypass android
```

Copy the `SHA1` value into Firebase Console > **Project settings > Your apps > Android app > SHA certificate fingerprints**.

7. Go to **Project settings > General > Your apps**.
8. Add an Android app with package name:

```text
com.krishiscan.app
```

9. Download `google-services.json`.
10. Place it here:

```text
mobile-android/app/google-services.json
```

11. In Firebase Console, open **Build > Firestore Database** and create a database.
12. Open **Build > Storage** and create a storage bucket.
13. Copy your bucket name. It usually looks like:

```text
your-project-id.appspot.com
```

or:

```text
your-project-id.firebasestorage.app
```

## 2. Deploy Firebase Rules

From the project root:

```powershell
firebase login
firebase use your-firebase-project-id
firebase deploy --only firestore:rules,firestore:indexes,storage
```

Rules used by this project:

- Firestore rules: `infra/firebase/firestore.rules`
- Firestore indexes: `infra/firebase/firestore.indexes.json`
- Storage rules: `infra/firebase/storage.rules`

If `firebase deploy` says no project is selected, run:

```powershell
firebase projects:list
firebase use your-project-id
```

## 3. Create Firebase Admin Credentials For Backend

The backend verifies Firebase ID tokens using Firebase Admin SDK.

1. Firebase Console > **Project settings > Service accounts**.
2. Click **Generate new private key**.
3. Save the JSON file somewhere outside source control, for example:

```text
D:\secrets\krishiscan-service-account.json
```

4. Never commit this JSON file.

## 4. Configure Backend

Go to backend folder:

```powershell
cd backend-api
copy .env.example .env
```

Edit `backend-api/.env`:

```env
PORT=8080
GOOGLE_APPLICATION_CREDENTIALS=D:\secrets\krishiscan-service-account.json
FIREBASE_STORAGE_BUCKET=your-project-id.appspot.com
GEMINI_API_KEY=your_gemini_api_key
GEMINI_MODEL=gemini-1.5-flash
REDIS_URL=redis://localhost:6379
```

Install dependencies:

```powershell
npm install
```

Start Redis locally, then run backend:

```powershell
npm run dev
```

Backend local base URL:

```text
http://localhost:8080/api/v1/
```

Android emulator uses this host URL:

```text
http://10.0.2.2:8080/api/v1/
```

That value is already configured in `mobile-android/app/build.gradle.kts` for debug builds.

## 5.1 Auth And Profile Validation

Login/register validation happens before Firebase is called:

- Email is required.
- Email must be valid.
- Password is required.
- Password must be at least 6 characters.

Profile validation happens before the backend `PATCH /users/me` call:

- Name is required and must be at least 3 characters.
- Phone is optional, but if entered it must be a valid 10 digit Indian mobile number starting with 6, 7, 8, or 9.
- State is optional, but if entered it must be at least 2 characters.

On successful login/register, the app calls backend `POST /auth/session`. This creates the user profile document in Firestore if it does not already exist.

## 5. Configure Android App

Open this folder in Android Studio:

```text
mobile-android
```

Then:

1. Wait for Gradle sync.
2. Confirm `mobile-android/app/google-services.json` exists.
3. Confirm model assets exist:

```text
mobile-android/app/src/main/assets/krishiscan_model.tflite
mobile-android/app/src/main/assets/labels.txt
```

4. Run the app on an emulator or device.

For a physical Android device on the same Wi-Fi, change debug `API_BASE_URL` in `app/build.gradle.kts` from:

```kotlin
"http://10.0.2.2:8080/api/v1/"
```

to your computer LAN IP:

```kotlin
"http://192.168.1.25:8080/api/v1/"
```

## 6. Generate Or Replace The TFLite Model

Use the Colab notebook:

```text
mobile-android/KrishiScan_Colab_Training_A_to_Z.ipynb
```

Or run training locally with PowerShell:

```powershell
cd mobile-android
$env:KAGGLE_USERNAME = "your_kaggle_username"
$env:KAGGLE_KEY = "your_kaggle_key"
.\train_krishiscan.ps1 -CopyToAndroidAssets
```

Or with Bash:

```bash
cd mobile-android
export KAGGLE_USERNAME="your_kaggle_username"
export KAGGLE_KEY="your_kaggle_key"
COPY_TO_ANDROID_ASSETS=1 ./train_krishiscan.sh
```

Outputs are written to `mobile-android/training-work/export/`. With the copy flag enabled, the script also places `krishiscan_model.tflite` and `labels.txt` in `app/src/main/assets/`.

Important: `labels.txt` order must exactly match the model output order. If labels and model do not match, the app will run but predictions will be wrong.

## 7. Run Complete App Locally

Start backend:

```powershell
cd backend-api
npm run dev
```

Start Android:

1. Open `mobile-android` in Android Studio.
2. Select emulator.
3. Click Run.

Test flow:

1. Register using email/password or Google.
2. Open Scan.
3. Choose Gallery or Camera.
4. Tap Analyze.
5. Result screen should show prediction, confidence, severity, and treatment.
6. Tap Share to share the result text.
7. Return to Scan or Profile and open a history item.
8. Open Chat and send a message.
9. Open Profile and test logout.

## 8. Backend API Routes Used By App

All routes are under:

```text
/api/v1
```

The app sends Firebase ID token as:

```text
Authorization: Bearer <firebase-id-token>
```

Routes:

- `POST /auth/session`
- `GET /users/me`
- `PATCH /users/me`
- `POST /uploads/scan-image`
- `POST /scans`
- `GET /scans`
- `GET /scans/:scanId`
- `DELETE /scans/:scanId`
- `POST /chat/messages`
- `GET /chat/messages`
- `POST /notifications/token`

## 9. Deploy Backend

Build backend:

```powershell
cd backend-api
npm install
npm run build
```

Deploy to Render, Railway, Fly.io, VM, or any Node host with these settings:

```text
Build command: npm install && npm run build
Start command: npm start
Node version: 18+
```

Production environment variables:

```env
PORT=8080
FIREBASE_STORAGE_BUCKET=your-project-id.appspot.com
GEMINI_API_KEY=your_gemini_api_key
GEMINI_MODEL=gemini-1.5-flash
REDIS_URL=your_production_redis_url
```

For Firebase Admin credentials in production, use one of these:

- Set `GOOGLE_APPLICATION_CREDENTIALS` to a service-account JSON path on your server.
- Or configure your hosting provider with Google Application Default Credentials.
- Or store the service account JSON as a secret file if your host supports secret files.

After deploy, update release API URL in `mobile-android/app/build.gradle.kts`:

```kotlin
release {
    buildConfigField("String", "API_BASE_URL", "\"https://your-backend-domain.com/api/v1/\"")
}
```

Then rebuild release APK.

## 10. Generate Debug APK

In Android Studio:

1. Open `mobile-android`.
2. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
3. APK output appears under:

```text
mobile-android/app/build/outputs/apk/debug/app-debug.apk
```

Command line if Gradle is installed:

```powershell
cd mobile-android
gradle assembleDebug
```

## 11. Generate Release APK

Create a keystore:

```powershell
keytool -genkeypair -v -keystore krishiscan-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias krishiscan
```

In Android Studio:

1. Open **Build > Generate Signed Bundle / APK**.
2. Choose **APK**.
3. Select `krishiscan-release.jks`.
4. Enter alias `krishiscan`.
5. Choose `release`.
6. Click Finish.

Release APK output:

```text
mobile-android/app/release/app-release.apk
```

If using command line, first configure signing in Android Studio or Gradle, then run:

```powershell
cd mobile-android
gradle assembleRelease
```

## 12. Common Issues

`google-services.json is missing`

- Download it from Firebase Console and place it in `mobile-android/app/google-services.json`.

`default_web_client_id not found`

- Enable Google Sign-In in Firebase Auth.
- Re-download `google-services.json`.
- Sync Gradle again.

`Network error` on emulator

- Start backend with `npm run dev`.
- Keep debug URL as `http://10.0.2.2:8080/api/v1/`.
- Confirm Windows firewall allows Node.js.

`Network error` on physical phone

- Use your computer LAN IP in debug `API_BASE_URL`.
- Phone and computer must be on same Wi-Fi.
- Backend must listen on port `8080`.

`Invalid or expired token`

- Make sure the Android app uses the same Firebase project as backend credentials.
- Confirm `GOOGLE_APPLICATION_CREDENTIALS` points to the correct service-account JSON.

`Image upload failed`

- Check `FIREBASE_STORAGE_BUCKET`.
- Deploy Storage rules.
- Confirm backend service account has Firebase Storage access.

`AI service unavailable`

- Check `GEMINI_API_KEY`.
- Confirm backend has internet access.
- Check Gemini quota.

`Wrong disease prediction`

- Ensure `labels.txt` matches `krishiscan_model.tflite`.
- Retrain and copy both files together.

## Final Checklist Before Submission

- `google-services.json` added locally.
- Firebase Auth providers enabled.
- Firestore database created.
- Storage bucket created.
- Firebase rules deployed.
- Backend `.env` filled.
- Redis running locally or production Redis configured.
- Gemini API key added.
- Android debug API URL correct.
- Model and labels present in assets.
- Auth, Scan, Result, Chat, Profile tested.
- Release backend URL configured before release APK.
- Signed release APK generated.
