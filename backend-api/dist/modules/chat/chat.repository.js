"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ChatRepository = void 0;
const crypto_1 = require("crypto");
const firestore_client_1 = require("../../infra/firestore/firestore-client");
class ChatRepository {
    async createUserMessage(uid, text) {
        const messageId = (0, crypto_1.randomUUID)();
        await firestore_client_1.firestore.collection('chats').doc(uid).collection('messages').doc(messageId).set({
            messageId,
            sender: 'user',
            text,
            status: 'sent',
            createdAt: firestore_client_1.admin.firestore.FieldValue.serverTimestamp(),
        });
        return { messageId };
    }
    async createAiMessage(uid, text) {
        const messageId = (0, crypto_1.randomUUID)();
        const createdAt = new Date().toISOString();
        await firestore_client_1.firestore.collection('chats').doc(uid).collection('messages').doc(messageId).set({
            messageId,
            sender: 'ai',
            text,
            status: 'sent',
            createdAt: firestore_client_1.admin.firestore.FieldValue.serverTimestamp(),
        });
        return { messageId, createdAt };
    }
    async list(uid, limit, cursor) {
        let q = firestore_client_1.firestore.collection('chats').doc(uid).collection('messages').orderBy('createdAt', 'desc').limit(limit);
        if (cursor) {
            const c = await firestore_client_1.firestore.collection('chats').doc(uid).collection('messages').doc(cursor).get();
            if (c.exists)
                q = q.startAfter(c);
        }
        const snap = await q.get();
        const items = snap.docs.map((d) => d.data()).reverse();
        const nextCursor = snap.docs.length === limit ? snap.docs[snap.docs.length - 1].id : null;
        return { items, nextCursor };
    }
    async getRecentContext(uid, limit) {
        const snap = await firestore_client_1.firestore.collection('chats').doc(uid).collection('messages').orderBy('createdAt', 'desc').limit(limit).get();
        return snap.docs
            .map((d) => d.data())
            .reverse()
            .map((m) => ({ role: m.sender === 'ai' ? 'model' : 'user', text: String(m.text || '') }));
    }
}
exports.ChatRepository = ChatRepository;
