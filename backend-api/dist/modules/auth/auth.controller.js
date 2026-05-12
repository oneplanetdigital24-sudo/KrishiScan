"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthController = void 0;
class AuthController {
    authService;
    constructor(authService) {
        this.authService = authService;
    }
    postSession = async (req, res, next) => {
        try {
            const payload = await this.authService.getSession(req.user.uid, req.user?.email);
            res.status(200).json(payload);
        }
        catch (err) {
            next(err);
        }
    };
}
exports.AuthController = AuthController;
