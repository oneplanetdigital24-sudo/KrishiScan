"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UserController = void 0;
class UserController {
    userService;
    constructor(userService) {
        this.userService = userService;
    }
    getMe = async (req, res, next) => {
        try {
            const result = await this.userService.getMe(req.user.uid);
            res.status(200).json(result);
        }
        catch (err) {
            next(err);
        }
    };
    patchMe = async (req, res, next) => {
        try {
            const result = await this.userService.updateMe(req.user.uid, req.body);
            res.status(200).json(result);
        }
        catch (err) {
            next(err);
        }
    };
    saveFcmToken = async (req, res, next) => {
        try {
            await this.userService.saveNotificationToken(req.user.uid, req.body.fcmToken);
            res.status(204).send();
        }
        catch (err) {
            next(err);
        }
    };
}
exports.UserController = UserController;
