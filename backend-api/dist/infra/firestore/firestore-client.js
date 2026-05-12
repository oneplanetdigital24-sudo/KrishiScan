"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.admin = exports.auth = exports.storage = exports.firestore = void 0;
const firebase_admin_1 = __importDefault(require("firebase-admin"));
exports.admin = firebase_admin_1.default;
const env_1 = require("../../common/config/env");
function normalizeStorageBucket(bucket) {
    return bucket.replace(/^gs:\/\//, '');
}
function buildCredential() {
    if (env_1.config.firebaseServiceAccountJson) {
        try {
            const serviceAccount = JSON.parse(env_1.config.firebaseServiceAccountJson);
            return firebase_admin_1.default.credential.cert(serviceAccount);
        }
        catch (error) {
            throw new Error(`Invalid FIREBASE_SERVICE_ACCOUNT_JSON: ${error instanceof Error ? error.message : 'Unable to parse JSON'}`);
        }
    }
    if (env_1.config.firebaseProjectId && env_1.config.firebaseClientEmail && env_1.config.firebasePrivateKey) {
        return firebase_admin_1.default.credential.cert({
            projectId: env_1.config.firebaseProjectId,
            clientEmail: env_1.config.firebaseClientEmail,
            privateKey: env_1.config.firebasePrivateKey.replace(/\\n/g, '\n'),
        });
    }
    if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
        return firebase_admin_1.default.credential.applicationDefault();
    }
    return undefined;
}
if (!firebase_admin_1.default.apps.length) {
    const credential = buildCredential();
    const appOptions = {
        storageBucket: normalizeStorageBucket(env_1.config.firebaseStorageBucket),
    };
    if (credential) {
        appOptions.credential = credential;
    }
    firebase_admin_1.default.initializeApp(appOptions);
}
exports.firestore = firebase_admin_1.default.firestore();
exports.storage = firebase_admin_1.default.storage().bucket();
exports.auth = firebase_admin_1.default.auth();
