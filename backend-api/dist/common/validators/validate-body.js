"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.validateBody = validateBody;
const api_error_1 = require("../errors/api-error");
function validateBody(schema) {
    return (req, _res, next) => {
        const parsed = schema.safeParse(req.body);
        if (!parsed.success) {
            next(new api_error_1.ApiError('VALIDATION_ERROR', parsed.error.issues.map((x) => x.message).join('; '), 400));
            return;
        }
        req.body = parsed.data;
        next();
    };
}
