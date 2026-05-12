import { AuthRepository } from './auth.repository';

export class AuthService {
  constructor(private readonly authRepository: AuthRepository) {}

  async getSession(uid: string, email?: string): Promise<Record<string, unknown>> {
    let user = await this.authRepository.findUserById(uid);
    if (!user) user = await this.authRepository.upsertMinimalUser(uid, email);
    return { user };
  }
}
