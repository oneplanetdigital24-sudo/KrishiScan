"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UploadService = void 0;
class UploadService {
    storageClient;
    constructor(storageClient) {
        this.storageClient = storageClient;
    }
    uploadScanImage(uid, fileBuffer) {
        return this.storageClient.uploadJpeg(uid, fileBuffer);
    }
}
exports.UploadService = UploadService;
