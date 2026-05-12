import { Request, Response, NextFunction } from 'express';
import { AuthService } from './auth.service';

export class AuthController {
  constructor(private readonly authService: AuthService) {}

  postSession = async (req: Request & { user?: { uid: string; email?: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      const payload = await this.authService.getSession(req.user!.uid, req.user?.email);
      res.status(200).json(payload);
    } catch (err) {
      next(err);
    }
  };
}
