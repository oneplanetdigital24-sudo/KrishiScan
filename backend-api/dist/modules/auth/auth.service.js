"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthService = void 0;
class AuthService {
    authRepository;
    constructor(authRepository) {
        this.authRepository = authRepository;
    }
    async getSession(uid, email) {
        let user = await this.authRepository.findUserById(uid);
        if (!user)
            user = await this.authRepository.upsertMinimalUser(uid, email);
        return { user };
    }
}
exports.AuthService = AuthService;
