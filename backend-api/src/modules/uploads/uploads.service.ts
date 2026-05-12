import { StorageClient } from '../../infra/storage/storage-client';

export class UploadService {
  constructor(private readonly storageClient: StorageClient) {}

  uploadScanImage(uid: string, fileBuffer: Buffer): Promise<{ imageUrl: string; path: string }> {
    return this.storageClient.uploadJpeg(uid, fileBuffer);
  }
}
