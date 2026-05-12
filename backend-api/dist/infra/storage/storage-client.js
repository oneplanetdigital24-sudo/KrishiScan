"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.StorageClient = void 0;
const crypto_1 = require("crypto");
const firestore_client_1 = require("../firestore/firestore-client");
class StorageClient {
    async uploadJpeg(uid, buffer) {
        const id = (0, crypto_1.randomUUID)();
        const path = `users/${uid}/scans/${id}.jpg`;
        const file = firestore_client_1.storage.file(path);
        await file.save(buffer, { resumable: false, contentType: 'image/jpeg' });
        const [signedUrl] = await file.getSignedUrl({
            action: 'read',
            expires: '2100-01-01',
        });
        return { imageUrl: signedUrl, path };
    }
    async deleteByPath(path) {
        try {
            await firestore_client_1.storage.file(path).delete();
        }
        catch {
            // ignore
        }
    }
}
exports.StorageClient = StorageClient;
