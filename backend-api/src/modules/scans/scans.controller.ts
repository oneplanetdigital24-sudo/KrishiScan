import { Request, Response, NextFunction } from 'express';
import { ScanService } from './scans.service';

export class ScanController {
  constructor(private readonly scanService: ScanService) {}

  create = async (req: Request & { user?: { uid: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      const result = await this.scanService.create(req.user!.uid, req.body);
      res.status(201).json(result);
    } catch (err) {
      next(err);
    }
  };

  list = async (req: Request & { user?: { uid: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      const limit = Number(req.query.limit ?? 20);
      const cursor = req.query.cursor as string | undefined;
      const crop = req.query.crop as string | undefined;
      const minConfidence = req.query.minConfidence ? Number(req.query.minConfidence) : undefined;
      const result = await this.scanService.list(req.user!.uid, limit, cursor, crop, minConfidence);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  };

  getOne = async (req: Request & { user?: { uid: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      const result = await this.scanService.get(req.user!.uid, req.params.scanId);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  };

  deleteOne = async (req: Request & { user?: { uid: string } }, res: Response, next: NextFunction): Promise<void> => {
    try {
      await this.scanService.delete(req.user!.uid, req.params.scanId);
      res.status(204).send();
    } catch (err) {
      next(err);
    }
  };
}
