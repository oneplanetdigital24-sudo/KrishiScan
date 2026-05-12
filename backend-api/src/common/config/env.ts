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
  geminiApiKey: must('GEMINI_API_KEY'),
  geminiModel: process.env.GEMINI_MODEL || 'gemini-1.5-flash',
  redisUrl: must('REDIS_URL'),
};
