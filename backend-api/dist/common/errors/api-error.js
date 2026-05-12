"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ApiError = void 0;
class ApiError extends Error {
    code;
    status;
    requestId;
    constructor(code, message, status, requestId) {
        super(message);
        this.code = code;
        this.status = status;
        this.requestId = requestId;
    }
}
exports.ApiError = ApiError;
