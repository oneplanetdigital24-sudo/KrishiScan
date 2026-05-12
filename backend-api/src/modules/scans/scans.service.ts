import { ApiError } from '../../common/errors/api-error';
import { GeminiClient } from '../../infra/gemini/gemini-client';
import { RedisRateLimiter } from '../../infra/redis/redis-rate-limiter';
import { StorageClient } from '../../infra/storage/storage-client';
import { CreateScanInput, ScanRepository } from './scans.repository';

export class ScanService {
  constructor(
    private readonly scanRepository: ScanRepository,
    private readonly geminiClient: GeminiClient,
    private readonly rateLimiter: RedisRateLimiter,
    private readonly storageClient: StorageClient,
  ) {}

  private resolveSeverity(confidence: number): 'mild' | 'moderate' | 'severe' {
    if (confidence >= 0.85) return 'severe';
    if (confidence >= 0.6) return 'moderate';
    return 'mild';
  }

  async create(uid: string, input: CreateScanInput): Promise<{ scanId: string; severity: 'mild' | 'moderate' | 'severe'; treatment: string | null }> {
    await this.rateLimiter.assertWithinLimit(`scan-treatment:${uid}`, 20, 600);
    const severity = this.resolveSeverity(input.confidence);

    let treatment: string | null = null;
    try {
      treatment = await this.geminiClient.generateTreatment(`${input.cropName} ${input.diseaseName} ${input.confidence}`);
    } catch {
      treatment = null;
    }

    const { scanId } = await this.scanRepository.create(uid, { ...input, severity, treatment });
    return { scanId, severity, treatment };
  }

  list(uid: string, limit: number, cursor?: string, crop?: string, minConfidence?: number): Promise<{ items: unknown[]; nextCursor: string | null }> {
    return this.scanRepository.list(uid, Math.min(Math.max(limit, 1), 50), cursor, crop, minConfidence);
  }

  async get(uid: string, scanId: string): Promise<Record<string, unknown>> {
    try {
      return await this.scanRepository.get(uid, scanId);
    } catch {
      throw new ApiError('NOT_FOUND', 'Scan not found', 404);
    }
  }

  async delete(uid: string, scanId: string): Promise<void> {
    try {
      const { imagePath } = await this.scanRepository.delete(uid, scanId);
      if (imagePath) await this.storageClient.deleteByPath(imagePath);
    } catch {
      throw new ApiError('NOT_FOUND', 'Scan not found', 404);
    }
  }
}
