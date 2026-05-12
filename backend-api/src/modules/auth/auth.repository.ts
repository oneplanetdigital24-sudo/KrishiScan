import { firestore, admin } from '../../infra/firestore/firestore-client';
import { serializeFirestoreDoc } from '../../common/utils/serialize-firestore';

export class AuthRepository {
  async findUserById(uid: string): Promise<Record<string, unknown> | null> {
    const snap = await firestore.collection('users').doc(uid).get();
    if (!snap.exists) return null;
    const data = snap.data();
    return data ? serializeFirestoreDoc(data) : null;
  }

  async upsertMinimalUser(uid: string, email?: string): Promise<Record<string, unknown>> {
    const ref = firestore.collection('users').doc(uid);
    const now = admin.firestore.FieldValue.serverTimestamp();
    await ref.set(
      {
        uid,
        email: email ?? null,
        name: 'Farmer',
        language: 'en',
        role: 'farmer',
        updatedAt: now,
        createdAt: now,
      },
      { merge: true },
    );
    const snap = await ref.get();
    return serializeFirestoreDoc(snap.data() as Record<string, unknown>);
  }
}
