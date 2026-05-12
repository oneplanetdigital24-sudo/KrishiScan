import dotenv from 'dotenv';
dotenv.config();

function must(name: string): string {
  const v = process.env[name];
  if (!v) throw new Error(`Missing environment variable: ${name}`);
  return v;
}

export const config = {
  port: Number(process.env.PORT || 8080),
  firebaseStorageBucket: must('FIREBASE_STORAGE_BUCKET'),
  firebaseServiceAccountJson: process.env.FIREBASE_SERVICE_ACCOUNT_JSON,
  firebaseProjectId: process.env.FIREBASE_PROJECT_ID,
  firebaseClientEmail: process.env.FIREBASE_CLIENT_EMAIL,
  firebasePrivateKey: process.env.FIREBASE_PRIVATE_KEY,
  geminiApiKey: must('GEMINI_API_KEY'),
  geminiModel: process.env.GEMINI_MODEL || 'gemini-2.5-flash',
  redisUrl: must('REDIS_URL'),
};
