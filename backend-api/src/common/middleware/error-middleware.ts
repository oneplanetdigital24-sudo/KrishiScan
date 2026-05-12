import { Request, Response, NextFunction } from 'express';
import { randomUUID } from 'crypto';
import { ApiError } from '../errors/api-error';

export function errorMiddleware(err: Error, req: Request, res: Response, _next: NextFunction): void {
  const requestId = req.header('x-request-id') || randomUUID();

  if (err instanceof ApiError) {
    res.status(err.status).json({
      error: { code: err.code, message: err.message, requestId: err.requestId || requestId },
    });
    return;
  }

  res.status(500).json({
    error: { code: 'INTERNAL_ERROR', message: 'Something went wrong', requestId },
  });
}
