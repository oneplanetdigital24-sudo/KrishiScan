import { randomUUID } from 'crypto';
import { storage } from '../firestore/firestore-client';

export class StorageClient {
  async uploadJpeg(uid: string, buffer: Buffer): Promise<{ imageUrl: string; path: string }> {
    const id = randomUUID();
    const path = `users/${uid}/scans/${id}.jpg`;
    const file = storage.file(path);
    await file.save(buffer, { resumable: false, contentType: 'image/jpeg' });

    const [signedUrl] = await file.getSignedUrl({
      action: 'read',
      expires: '2100-01-01',
    });

    return { imageUrl: signedUrl, path };
  }

  async deleteByPath(path: string): Promise<void> {
    try {
      await storage.file(path).delete();
    } catch {
      // ignore
    }
  }
}
