"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthRepository = void 0;
const firestore_client_1 = require("../../infra/firestore/firestore-client");
class AuthRepository {
    async findUserById(uid) {
        const snap = await firestore_client_1.firestore.collection('users').doc(uid).get();
        if (!snap.exists)
            return null;
        return snap.data() ?? null;
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
        return snap.data();
    }
}
exports.AuthRepository = AuthRepository;
