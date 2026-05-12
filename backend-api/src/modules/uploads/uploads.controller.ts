import { Request, Response, NextFunction } from 'express';
import { ApiError } from '../../common/errors/api-error';
import { UploadService } from './uploads.service';

export class UploadController {
  constructor(private readonly uploadService: UploadService) {}

  uploadScanImage = async (req: Request & { user?: { uid: string }; file?: Express.Multer.File }, res: Response, next: NextFunction): Promise<void> => {
    try {
      if (!req.file?.buffer) throw new ApiError('VALIDATION_ERROR', 'Image file is required', 400);
      const result = await this.uploadService.uploadScanImage(req.user!.uid, req.file.buffer);
      res.status(200).json(result);
    } catch (err) {
      next(err);
    }
  };
}
