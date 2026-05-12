"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.config = void 0;
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
function must(name) {
    const v = process.env[name];
    if (!v)
        throw new Error(`Missing environment variable: ${name}`);
    return v;
}
exports.config = {
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
