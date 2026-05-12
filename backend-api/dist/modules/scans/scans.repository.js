"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ScanRepository = void 0;
const crypto_1 = require("crypto");
const firestore_client_1 = require("../../infra/firestore/firestore-client");
const serialize_firestore_1 = require("../../common/utils/serialize-firestore");
class ScanRepository {
    async create(uid, input) {
        const scanId = (0, crypto_1.randomUUID)();
        const doc = {
            scanId,
            userId: uid,
            ...input,
            createdAt: firestore_client_1.admin.firestore.FieldValue.serverTimestamp(),
        };
        await firestore_client_1.firestore.collection('scans').doc(scanId).set(doc);
        return { scanId };
    }
    async list(uid, limit, cursor, crop, minConfidence) {
        let q = firestore_client_1.firestore.collection('scans').where('userId', '==', uid);
        if (crop)
            q = q.where('cropName', '==', crop);
        if (typeof minConfidence === 'number')
            q = q.where('confidence', '>=', minConfidence);
        q = q.orderBy('createdAt', 'desc').limit(limit);
        if (cursor) {
            const c = await firestore_client_1.firestore.collection('scans').doc(cursor).get();
            if (c.exists)
                q = q.startAfter(c);
        }
        const snap = await q.get();
        const items = snap.docs.map((d) => (0, serialize_firestore_1.serializeFirestoreDoc)(d.data()));
        const nextCursor = snap.docs.length === limit ? snap.docs[snap.docs.length - 1].id : null;
        return { items, nextCursor };
    }
    async get(uid, scanId) {
        const snap = await firestore_client_1.firestore.collection('scans').doc(scanId).get();
        const data = snap.data();
        if (!data || data.userId !== uid)
            throw new Error('Scan not found');
        return (0, serialize_firestore_1.serializeFirestoreDoc)(data);
    }
    async delete(uid, scanId) {
        const ref = firestore_client_1.firestore.collection('scans').doc(scanId);
        const snap = await ref.get();
        const data = snap.data();
        if (!data || data.userId !== uid)
            throw new Error('Scan not found');
        await ref.delete();
        return { imagePath: typeof data.imagePath === 'string' ? data.imagePath : undefined };
    }
}
exports.ScanRepository = ScanRepository;
