"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.admin = exports.auth = exports.storage = exports.firestore = void 0;
const firebase_admin_1 = __importDefault(require("firebase-admin"));
exports.admin = firebase_admin_1.default;
const env_1 = require("../../common/config/env");
if (!firebase_admin_1.default.apps.length) {
    firebase_admin_1.default.initializeApp({
        storageBucket: env_1.config.firebaseStorageBucket,
    });
}
exports.firestore = firebase_admin_1.default.firestore();
exports.storage = firebase_admin_1.default.storage().bucket();
exports.auth = firebase_admin_1.default.auth();
