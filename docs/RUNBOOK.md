# KrishiScan Runbook (Restarted)

## 1) Backend setup
Path: `backend-api`

1. Copy env file:
   - `cp .env.example .env` (or create manually on Windows)
2. Fill values in `.env`:
   - `FIREBASE_STORAGE_BUCKET=<your-bucket>.appspot.com`
   - `GEMINI_API_KEY=<your-key>`
   - `REDIS_URL=redis://localhost:6379`
   - `PORT=8080`
3. Ensure Firebase Admin credentials are available (service account via `GOOGLE_APPLICATION_CREDENTIALS` or default ADC).
4. Install and run:
   - `npm install`
   - `npm run dev`

## 2) Android setup
Path: `mobile-android`

1. Place `google-services.json` inside `mobile-android/app/`.
2. Open `mobile-android` in Android Studio.
3. Sync Gradle.

## 3) Base URL behavior
- `debug` build uses: `http://10.0.2.2:8080/api/v1/` (Android emulator -> host machine)
- `release` build uses: `https://api.krishiscan.com/api/v1/`

For physical device on same Wi-Fi, replace debug `API_BASE_URL` in `app/build.gradle.kts` with host LAN IP, for example:
- `http://192.168.1.25:8080/api/v1/`

## 4) First test flow
1. Launch app.
2. Register/Login.
3. Open Scan tab.
4. Pick gallery image or camera preview.
5. Tap Analyze.
6. Verify result screen opens with severity + treatment.
7. Open Chat tab and send message.

## 5) If upload fails
- Confirm backend is running and reachable.
- Confirm `android:usesCleartextTraffic="true"` is present in manifest for HTTP local testing.
- Confirm backend route `/api/v1/uploads/scan-image` exists and Firebase Storage bucket is valid.
