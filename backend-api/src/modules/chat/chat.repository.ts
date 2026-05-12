import { randomUUID } from 'crypto';
import { firestore, admin } from '../../infra/firestore/firestore-client';
import { serializeFirestoreDoc } from '../../common/utils/serialize-firestore';

export class ChatRepository {
  async createUserMessage(uid: string, text: string): Promise<{ messageId: string }> {
    const messageId = randomUUID();
    await firestore.collection('chats').doc(uid).collection('messages').doc(messageId).set({
      messageId,
      sender: 'user',
      text,
      status: 'sent',
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return { messageId };
  }

  async createAiMessage(uid: string, text: string): Promise<{ messageId: string; createdAt: string }> {
    const messageId = randomUUID();
    const createdAt = new Date().toISOString();
    await firestore.collection('chats').doc(uid).collection('messages').doc(messageId).set({
      messageId,
      sender: 'ai',
      text,
      status: 'sent',
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return { messageId, createdAt };
  }

  async list(uid: string, limit: number, cursor?: string): Promise<{ items: unknown[]; nextCursor: string | null }> {
    let q: FirebaseFirestore.Query = firestore.collection('chats').doc(uid).collection('messages').orderBy('createdAt', 'desc').limit(limit);
    if (cursor) {
      const c = await firestore.collection('chats').doc(uid).collection('messages').doc(cursor).get();
      if (c.exists) q = q.startAfter(c);
    }
    const snap = await q.get();
    const items = snap.docs.map((d) => serializeFirestoreDoc(d.data())).reverse();
    const nextCursor = snap.docs.length === limit ? snap.docs[snap.docs.length - 1].id : null;
    return { items, nextCursor };
  }

  async getRecentContext(uid: string, limit: number): Promise<Array<{ role: 'user' | 'model'; text: string }>> {
    const snap = await firestore.collection('chats').doc(uid).collection('messages').orderBy('createdAt', 'desc').limit(limit).get();
    return snap.docs
      .map((d) => d.data())
      .reverse()
      .map((m) => ({ role: m.sender === 'ai' ? 'model' : 'user', text: String(m.text || '') }));
  }
}
