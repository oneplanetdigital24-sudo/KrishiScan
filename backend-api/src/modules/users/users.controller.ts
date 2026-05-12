import { Request, Response, NextFunction } from 'express';
import { UserService } from './users.service';

export class UserController {
  constructor(private readonly userService: UserService) {}

  getMe = async (req: Request & { user?: { uid: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      const result = await this.userService.getMe(req.user!.uid);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  };

  patchMe = async (req: Request & { user?: { uid: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      const result = await this.userService.updateMe(req.user!.uid, req.body);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  };

  saveFcmToken = async (req: Request & { user?: { uid: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      await this.userService.saveNotificationToken(req.user!.uid, req.body.fcmToken);
      res.status(204).send();
    } catch (err) {
      next(err);
    }
  };
}
