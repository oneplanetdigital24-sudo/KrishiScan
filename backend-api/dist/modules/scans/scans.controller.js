"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ScanController = void 0;
class ScanController {
    scanService;
    constructor(scanService) {
        this.scanService = scanService;
    }
    create = async (req, res, next) => {
        try {
            const result = await this.scanService.create(req.user.uid, req.body);
            res.status(201).json(result);
        }
        catch (err) {
            next(err);
        }
    };
    list = async (req, res, next) => {
        try {
            const limit = Number(req.query.limit ?? 20);
            const cursor = req.query.cursor;
            const crop = req.query.crop;
            const minConfidence = req.query.minConfidence ? Number(req.query.minConfidence) : undefined;
            const result = await this.scanService.list(req.user.uid, limit, cursor, crop, minConfidence);
            res.status(200).json(result);
        }
        catch (err) {
            next(err);
        }
    };
    getOne = async (req, res, next) => {
        try {
            const result = await this.scanService.get(req.user.uid, req.params.scanId);
            res.status(200).json(result);
        }
        catch (err) {
            next(err);
        }
    };
    deleteOne = async (req, res, next) => {
        try {
            await this.scanService.delete(req.user.uid, req.params.scanId);
            res.status(204).send();
        }
        catch (err) {
            next(err);
        }
    };
}
exports.ScanController = ScanController;
