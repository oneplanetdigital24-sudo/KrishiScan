import { createClient } from 'redis';
import { config } from '../../common/config/env';
import { ApiError } from '../../common/errors/api-error';

const redis = createClient({ url: config.redisUrl });
let redisReady = false;
redis.on('ready', () => {
  redisReady = true;
});
redis.on('end', () => {
  redisReady = false;
});
redis.on('error', () => {
  redisReady = false;
});
redis.connect().catch(() => {
  redisReady = false;
});

export class RedisRateLimiter {
  async assertWithinLimit(key: string, maxRequests: number, windowSeconds: number): Promise<void> {
    if (!redisReady) return;

    try {
      const count = await redis.incr(key);
      if (count === 1) {
        await redis.expire(key, windowSeconds);
      }
      if (count > maxRequests) {
        throw new ApiError('RATE_LIMITED', 'Too many requests', 429);
      }
    } catch (err) {
      if (err instanceof ApiError) throw err;
      redisReady = false;
    }
  }
}
