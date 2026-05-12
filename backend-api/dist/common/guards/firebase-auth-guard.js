"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.firebaseAuthGuard = firebaseAuthGuard;
const api_error_1 = require("../errors/api-error");
const firestore_client_1 = require("../../infra/firestore/firestore-client");
async function firebaseAuthGuard(req, _res, next) {
    try {
        const authHeader = req.header('authorization');
        if (!authHeader?.startsWith('Bearer '))
            throw new api_error_1.ApiError('UNAUTHORIZED', 'Missing bearer token', 401);
        const token = authHeader.replace('Bearer ', '').trim();
        if (!token)
            throw new api_error_1.ApiError('UNAUTHORIZED', 'Invalid bearer token', 401);
        const decoded = await firestore_client_1.auth.verifyIdToken(token, true);
        req.user = {
            uid: decoded.uid,
            email: decoded.email,
        };
        next();
    }
    catch {
        next(new api_error_1.ApiError('UNAUTHORIZED', 'Invalid or expired token', 401));
    }
}
