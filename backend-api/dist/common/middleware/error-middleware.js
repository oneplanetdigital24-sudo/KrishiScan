"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.errorMiddleware = errorMiddleware;
const crypto_1 = require("crypto");
const api_error_1 = require("../errors/api-error");
function errorMiddleware(err, req, res, _next) {
    const requestId = req.header('x-request-id') || (0, crypto_1.randomUUID)();
    if (err instanceof api_error_1.ApiError) {
        res.status(err.status).json({
            error: { code: err.code, message: err.message, requestId: err.requestId || requestId },
        });
        return;
    }
    res.status(500).json({
        error: { code: 'INTERNAL_ERROR', message: 'Something went wrong', requestId },
    });
}
