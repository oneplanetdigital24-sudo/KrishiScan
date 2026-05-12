"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UploadController = void 0;
const api_error_1 = require("../../common/errors/api-error");
class UploadController {
    uploadService;
    constructor(uploadService) {
        this.uploadService = uploadService;
    }
    uploadScanImage = async (req, res, next) => {
        try {
            if (!req.file?.buffer)
                throw new api_error_1.ApiError('VALIDATION_ERROR', 'Image file is required', 400);
            const result = await this.uploadService.uploadScanImage(req.user.uid, req.file.buffer);
            res.status(200).json(result);
        }
        catch (err) {
            next(err);
        }
    };
}
exports.UploadController = UploadController;
