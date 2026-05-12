import { randomUUID } from 'crypto';
import { firestore, admin } from '../../infra/firestore/firestore-client';

export type CreateScanInput = {
  cropName: string;
  diseaseName: string;
  confidence: number;
  imageUrl: string;
  imagePath?: string;
  location?: { lat: number; lng: number } | null;
};

export class ScanRepository {
  async create(uid: string, input: CreateScanInput & { severity: 'mild' | 'moderate' | 'severe'; treatment: string | null }): Promise<{ scanId: string }> {
    const scanId = randomUUID();
    const doc = {
      scanId,
      userId: uid,
      ...input,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };
    await firestore.collection('scans').doc(scanId).set(doc);
    return { scanId };
  }

  async list(uid: string, limit: number, cursor?: string, crop?: string, minConfidence?: number): Promise<{ items: unknown[]; nextCursor: string | null }> {
    let q: FirebaseFirestore.Query = firestore.collection('scans').where('userId', '==', uid);
    if (crop) q = q.where('cropName', '==', crop);
    if (typeof minConfidence === 'number') q = q.where('confidence', '>=', minConfidence);
    q = q.orderBy('createdAt', 'desc').limit(limit);

    if (cursor) {
      const c = await firestore.collection('scans').doc(cursor).get();
      if (c.exists) q = q.startAfter(c);
    }

    const snap = await q.get();
    const items = snap.docs.map((d) => d.data());
    const nextCursor = snap.docs.length === limit ? snap.docs[snap.docs.length - 1].id : null;
    return { items, nextCursor };
  }

  async get(uid: string, scanId: string): Promise<Record<string, unknown>> {
    const snap = await firestore.collection('scans').doc(scanId).get();
    const data = snap.data();
    if (!data || data.userId !== uid) throw new Error('Scan not found');
    return data;
  }

  async delete(uid: string, scanId: string): Promise<{ imagePath?: string }> {
    const ref = firestore.collection('scans').doc(scanId);
    const snap = await ref.get();
    const data = snap.data();
    if (!data || data.userId !== uid) throw new Error('Scan not found');
    await ref.delete();
    return { imagePath: typeof data.imagePath === 'string' ? data.imagePath : undefined };
  }
}
