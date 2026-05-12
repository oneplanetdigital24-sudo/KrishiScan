import { Request, Response, NextFunction } from 'express';
import { ZodSchema } from 'zod';
import { ApiError } from '../errors/api-error';

export function validateBody<T>(schema: ZodSchema<T>) {
  return (req: Request, _res: Response, next: NextFunction): void => {
    const parsed = schema.safeParse(req.body);
    if (!parsed.success) {
      next(new ApiError('VALIDATION_ERROR', parsed.error.issues.map((x) => x.message).join('; '), 400));
      return;
    }
    req.body = parsed.data;
    next();
  };
}
