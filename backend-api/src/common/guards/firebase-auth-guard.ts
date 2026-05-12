import { Request, Response, NextFunction } from 'express';
import { ApiError } from '../errors/api-error';
import { auth } from '../../infra/firestore/firestore-client';

export async function firebaseAuthGuard(req: Request, _res: Response, next: NextFunction): Promise<void> {
  try {
    const authHeader = req.header('authorization');
    if (!authHeader?.startsWith('Bearer ')) throw new ApiError('UNAUTHORIZED', 'Missing bearer token', 401);

    const token = authHeader.replace('Bearer ', '').trim();
    if (!token) throw new ApiError('UNAUTHORIZED', 'Invalid bearer token', 401);

    const decoded = await auth.verifyIdToken(token, true);
    (req as Request & { user?: { uid: string; email?: string } }).user = {
      uid: decoded.uid,
      email: decoded.email,
    };
    next();
  } catch {
    next(new ApiError('UNAUTHORIZED', 'Invalid or expired token', 401));
  }
}
