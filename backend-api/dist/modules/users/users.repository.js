"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UserRepository = void 0;
const firestore_client_1 = require("../../infra/firestore/firestore-client");
class UserRepository {
    async getById(uid) {
        const snap = await firestore_client_1.firestore.collection('users').doc(uid).get();
        return (snap.data() ?? { uid });
    }
    async updateById(uid, input) {
        const ref = firestore_client_1.firestore.collection('users').doc(uid);
        await ref.set({ ...input, updatedAt: firestore_client_1.admin.firestore.FieldValue.serverTimestamp() }, { merge: true });
        const snap = await ref.get();
        return (snap.data() ?? { uid });
    }
    async saveFcmToken(uid, fcmToken) {
        await firestore_client_1.firestore.collection('users').doc(uid).set({ fcmToken, updatedAt: firestore_client_1.admin.firestore.FieldValue.serverTimestamp() }, { merge: true });
    }
}
exports.UserRepository = UserRepository;
