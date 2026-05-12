"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthRepository = void 0;
const firestore_client_1 = require("../../infra/firestore/firestore-client");
const serialize_firestore_1 = require("../../common/utils/serialize-firestore");
class AuthRepository {
    async findUserById(uid) {
        const snap = await firestore_client_1.firestore.collection('users').doc(uid).get();
        if (!snap.exists)
            return null;
        const data = snap.data();
        return data ? (0, serialize_firestore_1.serializeFirestoreDoc)(data) : null;
    }
    async upsertMinimalUser(uid, email) {
        const ref = firestore_client_1.firestore.collection('users').doc(uid);
        const now = firestore_client_1.admin.firestore.FieldValue.serverTimestamp();
        await ref.set({
            uid,
            email: email ?? null,
            name: 'Farmer',
            language: 'en',
            role: 'farmer',
            updatedAt: now,
            createdAt: now,
        }, { merge: true });
        const snap = await ref.get();
        return (0, serialize_firestore_1.serializeFirestoreDoc)(snap.data());
    }
}
exports.AuthRepository = AuthRepository;
