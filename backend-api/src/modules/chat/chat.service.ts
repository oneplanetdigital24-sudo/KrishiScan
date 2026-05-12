import { GeminiClient } from '../../infra/gemini/gemini-client';
import { RedisRateLimiter } from '../../infra/redis/redis-rate-limiter';
import { ChatRepository } from './chat.repository';

export class ChatService {
  constructor(
    private readonly chatRepository: ChatRepository,
    private readonly geminiClient: GeminiClient,
    private readonly rateLimiter: RedisRateLimiter,
  ) {}

  async sendMessage(uid: string, text: string): Promise<{ messageId: string; reply: string; createdAt: string }> {
    await this.rateLimiter.assertWithinLimit(`chat:${uid}`, 30, 600);

    await this.chatRepository.createUserMessage(uid, text);
    const context = await this.chatRepository.getRecentContext(uid, 10);
    const prompt = JSON.stringify({ context, user: text });
    let reply: string;
    try {
      reply = await this.geminiClient.generateChatReply(prompt);
    } catch {
      reply = 'I could not reach the AI service right now. Please check leaf color, spots, watering, and pests, then try again with the crop name and symptoms.';
    }
    const aiMessage = await this.chatRepository.createAiMessage(uid, reply);

    return { messageId: aiMessage.messageId, reply, createdAt: aiMessage.createdAt };
  }

  list(uid: string, limit: number, cursor?: string): Promise<{ items: unknown[]; nextCursor: string | null }> {
    return this.chatRepository.list(uid, Math.min(Math.max(limit, 1), 100), cursor);
  }
}
