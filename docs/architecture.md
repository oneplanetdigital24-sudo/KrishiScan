# KrishiScan Production Starter Pack

## Components
- Android app: MVVM, CameraX, TFLite inference, Retrofit API client, offline cache.
- Backend API: Express-based modules with controller -> service -> repository layering.
- Data: Firestore + Storage + Firebase Auth + FCM.
- AI: Gemini called only from backend.

## Request Flows
1. Auth session:
- Android obtains Firebase ID token.
- Calls `POST /api/v1/auth/session`.
- Backend verifies token and returns profile from `users/{uid}`.

2. Scan + treatment:
- Android runs TFLite on-device.
- Upload image to backend upload route (or signed upload path).
- Calls `POST /api/v1/scans` with prediction payload.
- Backend applies limits, calls Gemini, saves scan, returns treatment.

3. Chat:
- Android calls `POST /api/v1/chat/messages`.
- Backend saves user message, loads recent context, calls Gemini, stores AI response.

## Scale Notes (10k+ users)
- Stateless backend on Cloud Run with autoscaling.
- Rate limiting in Redis.
- Firestore cursor-based pagination for scans/chat.
- Keep chat context bounded (last 10 messages) to cap LLM token cost.
- Add async queue for non-critical tasks (analytics, notifications).

## Important TODOs
- Replace mock Firebase token verification in auth guard with Firebase Admin SDK.
- Add runtime validation (Zod/Joi) for all request DTOs.
- Add upload endpoint implementation for multipart image ingestion.
- Add structured logs and distributed request IDs.
