# KrishiScan Backend Render Free Deployment

This guide deploys `backend-api` on Render free tier from the beginning, including Firebase Admin, Gemini, Redis-compatible Render Key Value, environment variables, health check, and Android app configuration.

Render references:

- Web services: https://render.com/docs/web-services
- Free tier: https://render.com/docs/free
- Environment variables and secret files: https://render.com/docs/configure-environment-variables
- Key Value / Redis-compatible service: https://render.com/docs/key-value
- Health checks: https://render.com/docs/health-checks

## What You Will Deploy

- Service type: Render Web Service
- Runtime: Node
- Project folder: `backend-api`
- Build command: `npm install && npm run build`
- Start command: `npm start`
- Health check path: `/health`
- Public API base URL after deploy:

```text
https://your-render-service-name.onrender.com/api/v1/
```

Important: Render free services are good for testing and demos. They are not recommended for production. Free services can sleep after inactivity, so the first request after sleep can be slow.

## 1. Prepare The Code

Make sure these scripts exist in `backend-api/package.json`:

```json
{
  "scripts": {
    "dev": "tsx watch src/server.ts",
    "build": "tsc -p tsconfig.json",
    "start": "node dist/server.js",
    "typecheck": "tsc --noEmit"
  }
}
```

The backend already reads `PORT` from the environment, and `src/server.ts` binds to `0.0.0.0`, which Render expects for public web services.

Test locally:

```powershell
cd backend-api
npm install
npm run typecheck
npm run build
npm start
```

Open:

```text
http://localhost:8080/health
```

Expected response:

```json
{"ok":true,"service":"krishiscan-backend-api"}
```

## 2. Push Project To GitHub

Render deploys from GitHub, GitLab, or Bitbucket.

From project root:

```powershell
git init
git add .
git commit -m "Initial KrishiScan deployment"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git push -u origin main
```

Do not commit secrets:

- Do not commit `.env`.
- Do not commit Firebase service account JSON.
- Do not commit `google-services.json` if you want to keep Firebase app config private.

## 3. Create Firebase Service Account Secret

The backend uses Firebase Admin SDK to verify Firebase ID tokens and access Firestore/Storage.

1. Open Firebase Console.
2. Go to **Project settings > Service accounts**.
3. Click **Generate new private key**.
4. Download the JSON file.
5. Open the JSON file in a text editor and copy the full contents.

You will paste this into Render as a secret file.

## 4. Create Render Key Value Free Instance

The backend uses Redis for rate limiting. On Render, use Render Key Value.

1. Open Render Dashboard.
2. Click **New > Key Value**.
3. Name it:

```text
krishiscan-redis
```

4. Select the same region you will use for the backend web service.
5. Select the Free instance type.
6. Click **Create Key Value**.
7. After it becomes available, open **Connect**.
8. Copy the **Internal URL**.

It will look like:

```text
redis://red-xxxxx:6379
```

Use this value as `REDIS_URL` in the backend web service. Render recommends using the internal URL between services in the same region because it stays inside Render’s private network.

## 5. Create Render Web Service

1. Open Render Dashboard.
2. Click **New > Web Service**.
3. Connect your GitHub/GitLab/Bitbucket repository.
4. Select the branch:

```text
main
```

5. Configure service:

```text
Name: krishiscan-backend-api
Language: Node
Root Directory: backend-api
Branch: main
Region: same as Key Value
Instance Type: Free
Build Command: npm install && npm run build
Start Command: npm start
```

6. Open **Advanced** or service settings and set:

```text
Health Check Path: /health
```

Do not set health check path to `/api/v1/...` because API routes require Firebase Bearer auth and will return `401`.

## 6. Add Environment Variables

In your Render web service:

1. Open **Environment**.
2. Add these environment variables:

```env
NODE_VERSION=18
PORT=10000
FIREBASE_STORAGE_BUCKET=your-project-id.appspot.com
GEMINI_API_KEY=your_gemini_api_key
GEMINI_MODEL=gemini-1.5-flash
REDIS_URL=redis://your-render-key-value-internal-url
GOOGLE_APPLICATION_CREDENTIALS=/etc/secrets/firebase-service-account.json
```

Notes:

- Render sets a default port, but setting `PORT=10000` is clear and matches Render’s default web-service port.
- `FIREBASE_STORAGE_BUCKET` must match your Firebase Storage bucket.
- Use the internal Render Key Value URL for `REDIS_URL`.
- Keep `GEMINI_API_KEY` private.

## 7. Add Firebase Service Account As Secret File

In your Render web service:

1. Open **Environment**.
2. Scroll to **Secret Files**.
3. Click **Add Secret File**.
4. Filename:

```text
firebase-service-account.json
```

5. Contents: paste the full Firebase service account JSON.
6. Save changes.

Render exposes this file at:

```text
/etc/secrets/firebase-service-account.json
```

That path must match:

```env
GOOGLE_APPLICATION_CREDENTIALS=/etc/secrets/firebase-service-account.json
```

## 8. Deploy

Click **Create Web Service** or **Save, rebuild, and deploy**.

Render will run:

```text
npm install && npm run build
npm start
```

Wait until the deploy status becomes **Live**.

Your backend URL will look like:

```text
https://krishiscan-backend-api.onrender.com
```

Check health:

```text
https://krishiscan-backend-api.onrender.com/health
```

Expected:

```json
{"ok":true,"service":"krishiscan-backend-api"}
```

## 9. Update Android Release API URL

Open:

```text
mobile-android/app/build.gradle.kts
```

Change release URL:

```kotlin
release {
    isMinifyEnabled = false
    buildConfigField("String", "API_BASE_URL", "\"https://krishiscan-backend-api.onrender.com/api/v1/\"")
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

For debug builds using the deployed backend, you can also temporarily change debug:

```kotlin
debug {
    buildConfigField("String", "API_BASE_URL", "\"https://krishiscan-backend-api.onrender.com/api/v1/\"")
}
```

Then rebuild the Android app.

## 10. Firebase Setup Required For Android Login

The backend deployment alone is not enough. Android login/register also needs Firebase Android config.

In Firebase Console:

1. Enable **Authentication > Email/Password**.
2. Enable **Authentication > Google**.
3. Add Android app package:

```text
com.krishiscan.app
```

4. Add debug SHA1 fingerprint for Google Sign-In.
5. Download `google-services.json`.
6. Put it here:

```text
mobile-android/app/google-services.json
```

7. Sync Gradle in Android Studio.

## 11. Deploy Firebase Rules

From project root:

```powershell
firebase login
firebase use your-firebase-project-id
firebase deploy --only firestore:rules,firestore:indexes,storage
```

Rules are stored here:

```text
infra/firebase/firestore.rules
infra/firebase/firestore.indexes.json
infra/firebase/storage.rules
```

## 12. Test Full Flow

After Render is live and Android is configured:

1. Open:

```text
https://your-service.onrender.com/health
```

2. Run Android app.
3. Register a new user.
4. Confirm Firebase Auth now shows the user.
5. Confirm Firestore has a document in:

```text
users/{firebaseUid}
```

6. Open Profile and update name, phone, state.
7. Confirm Firestore user document updates.
8. Open Scan, upload image, and save result.
9. Confirm Storage receives image under:

```text
users/{firebaseUid}/scans/
```

10. Open Chat and send a farming question.

## 13. Troubleshooting

`Deploy failed during build`

- Check Root Directory is exactly `backend-api`.
- Check Build Command is `npm install && npm run build`.
- Open Render deploy logs and find the first TypeScript error.

`Service starts then exits`

- Check Start Command is `npm start`.
- Check `dist/server.js` exists after build.
- Check all required environment variables are set.

`Health check failed`

- Health Check Path must be `/health`.
- Do not use protected `/api/v1` routes as health checks.
- Confirm logs show `KrishiScan API listening on 10000`.

`Missing environment variable`

- Add missing value in Render **Environment**.
- Click **Save, rebuild, and deploy**.

`Invalid or expired token`

- Android `google-services.json` and backend service account must belong to the same Firebase project.
- Re-download `google-services.json` after enabling Google Sign-In and adding SHA1.

`Image upload failed`

- Check `FIREBASE_STORAGE_BUCKET`.
- Check Firebase service account secret file.
- Deploy Firebase Storage rules.

`Chat or treatment fails`

- Check `GEMINI_API_KEY`.
- Check Gemini quota.
- Check Render logs for `AI service unavailable`.

`Too many requests`

- The Redis rate limiter is working.
- Wait for the limiter window or increase limits in backend code.

`Redis connection issue`

- Use Render Key Value internal URL.
- Keep Key Value and Web Service in the same region.
- Confirm `REDIS_URL` starts with `redis://` or `rediss://`.

## Final Render Configuration Checklist

- GitHub repo connected.
- Web Service root directory is `backend-api`.
- Instance type is Free.
- Build command is `npm install && npm run build`.
- Start command is `npm start`.
- Health check path is `/health`.
- Render Key Value Free instance created.
- `REDIS_URL` set from Key Value internal URL.
- `FIREBASE_STORAGE_BUCKET` set.
- `GEMINI_API_KEY` set.
- Secret file `firebase-service-account.json` added.
- `GOOGLE_APPLICATION_CREDENTIALS=/etc/secrets/firebase-service-account.json`.
- Android release `API_BASE_URL` points to Render URL ending with `/api/v1/`.
