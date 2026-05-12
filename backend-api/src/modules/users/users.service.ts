import { UserRepository, UpdateUserInput } from './users.repository';

export class UserService {
  constructor(private readonly userRepository: UserRepository) {}

  getMe(uid: string): Promise<Record<string, unknown>> {
    return this.userRepository.getById(uid);
  }

  updateMe(uid: string, input: UpdateUserInput): Promise<Record<string, unknown>> {
    return this.userRepository.updateById(uid, input);
  }

  async saveNotificationToken(uid: string, fcmToken: string): Promise<void> {
    await this.userRepository.saveFcmToken(uid, fcmToken);
  }
}
