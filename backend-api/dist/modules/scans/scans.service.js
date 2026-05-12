"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ScanService = void 0;
const api_error_1 = require("../../common/errors/api-error");
class ScanService {
    scanRepository;
    geminiClient;
    rateLimiter;
    storageClient;
    constructor(scanRepository, geminiClient, rateLimiter, storageClient) {
        this.scanRepository = scanRepository;
        this.geminiClient = geminiClient;
        this.rateLimiter = rateLimiter;
        this.storageClient = storageClient;
    }
    resolveSeverity(confidence) {
        if (confidence >= 0.85)
            return 'severe';
        if (confidence >= 0.6)
            return 'moderate';
        return 'mild';
    }
    async create(uid, input) {
        await this.rateLimiter.assertWithinLimit(`scan-treatment:${uid}`, 20, 600);
        const severity = this.resolveSeverity(input.confidence);
        let treatment = null;
        try {
            treatment = await this.geminiClient.generateTreatment(`${input.cropName} ${input.diseaseName} ${input.confidence}`);
        }
        catch {
            treatment = null;
        }
        const { scanId } = await this.scanRepository.create(uid, { ...input, severity, treatment });
        return { scanId, severity, treatment };
    }
    list(uid, limit, cursor, crop, minConfidence) {
        return this.scanRepository.list(uid, Math.min(Math.max(limit, 1), 50), cursor, crop, minConfidence);
    }
    async get(uid, scanId) {
        try {
            return await this.scanRepository.get(uid, scanId);
        }
        catch {
            throw new api_error_1.ApiError('NOT_FOUND', 'Scan not found', 404);
        }
    }
    async delete(uid, scanId) {
        try {
            const { imagePath } = await this.scanRepository.delete(uid, scanId);
            if (imagePath)
                await this.storageClient.deleteByPath(imagePath);
        }
        catch {
            throw new api_error_1.ApiError('NOT_FOUND', 'Scan not found', 404);
        }
    }
}
exports.ScanService = ScanService;
