"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.RedisRateLimiter = void 0;
const redis_1 = require("redis");
const env_1 = require("../../common/config/env");
const api_error_1 = require("../../common/errors/api-error");
const redis = (0, redis_1.createClient)({ url: env_1.config.redisUrl });
redis.connect().catch(() => undefined);
class RedisRateLimiter {
    async assertWithinLimit(key, maxRequests, windowSeconds) {
        const count = await redis.incr(key);
        if (count === 1) {
            await redis.expire(key, windowSeconds);
        }
        if (count > maxRequests) {
            throw new api_error_1.ApiError('RATE_LIMITED', 'Too many requests', 429);
        }
    }
}
exports.RedisRateLimiter = RedisRateLimiter;
