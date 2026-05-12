"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ChatService = void 0;
class ChatService {
    chatRepository;
    geminiClient;
    rateLimiter;
    constructor(chatRepository, geminiClient, rateLimiter) {
        this.chatRepository = chatRepository;
        this.geminiClient = geminiClient;
        this.rateLimiter = rateLimiter;
    }
    async sendMessage(uid, text) {
        await this.rateLimiter.assertWithinLimit(`chat:${uid}`, 30, 600);
        await this.chatRepository.createUserMessage(uid, text);
        const context = await this.chatRepository.getRecentContext(uid, 10);
        const prompt = JSON.stringify({ context, user: text });
        let reply;
        try {
            reply = await this.geminiClient.generateChatReply(prompt);
        }
        catch {
            reply = 'I could not reach the AI service right now. Please check leaf color, spots, watering, and pests, then try again with the crop name and symptoms.';
        }
        const aiMessage = await this.chatRepository.createAiMessage(uid, reply);
        return { messageId: aiMessage.messageId, reply, createdAt: aiMessage.createdAt };
    }
    list(uid, limit, cursor) {
        return this.chatRepository.list(uid, Math.min(Math.max(limit, 1), 100), cursor);
    }
}
exports.ChatService = ChatService;
