"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.UserService = void 0;
class UserService {
    userRepository;
    constructor(userRepository) {
        this.userRepository = userRepository;
    }
    getMe(uid) {
        return this.userRepository.getById(uid);
    }
    updateMe(uid, input) {
        return this.userRepository.updateById(uid, input);
    }
    async saveNotificationToken(uid, fcmToken) {
        await this.userRepository.saveFcmToken(uid, fcmToken);
    }
}
exports.UserService = UserService;
