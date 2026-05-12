import { firestore, admin } from '../../infra/firestore/firestore-client';

export type UpdateUserInput = {
  name?: string;
  phone?: string | null;
  state?: string;
  language?: 'en' | 'hi' | 'as';
  role?: 'farmer' | 'student' | 'extension_worker';
};

export class UserRepository {
  async getById(uid: string): Promise<Record<string, unknown>> {
    const snap = await firestore.collection('users').doc(uid).get();
    return (snap.data() ?? { uid }) as Record<string, unknown>;
  }

  async updateById(uid: string, input: UpdateUserInput): Promise<Record<string, unknown>> {
    const ref = firestore.collection('users').doc(uid);
    await ref.set({ ...input, updatedAt: admin.firestore.FieldValue.serverTimestamp() }, { merge: true });
    const snap = await ref.get();
    return (snap.data() ?? { uid }) as Record<string, unknown>;
  }

  async saveFcmToken(uid: string, fcmToken: string): Promise<void> {
    await firestore.collection('users').doc(uid).set(
      { fcmToken, updatedAt: admin.firestore.FieldValue.serverTimestamp() },
      { merge: true },
    );
  }
}
