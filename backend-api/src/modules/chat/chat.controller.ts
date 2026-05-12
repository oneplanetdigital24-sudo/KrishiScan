import { Request, Response, NextFunction } from 'express';
import { ChatService } from './chat.service';

export class ChatController {
  constructor(private readonly chatService: ChatService) {}

  sendMessage = async (req: Request & { user?: { uid: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      const result = await this.chatService.sendMessage(req.user!.uid, req.body.text);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  };

  listMessages = async (req: Request & { user?: { uid: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      const limit = Number(req.query.limit ?? 50);
      const cursor = req.query.cursor as string | undefined;
      const result = await this.chatService.list(req.user!.uid, limit, cursor);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  };
}
