import { createClient } from 'redis';
import { config } from '../../common/config/env';
import { ApiError } from '../../common/errors/api-error';

const redis = createClient({ url: config.redisUrl });
redis.connect().catch(() => undefined);

export class RedisRateLimiter {
  async assertWithinLimit(key: string, maxRequests: number, windowSeconds: number): Promise<void> {
    const count = await redis.incr(key);
    if (count === 1) {
      await redis.expire(key, windowSeconds);
    }
    if (count > maxRequests) {
      throw new ApiError('RATE_LIMITED', 'Too many requests', 429);
    }
  }
}
