"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ChatController = void 0;
class ChatController {
    chatService;
    constructor(chatService) {
        this.chatService = chatService;
    }
    sendMessage = async (req, res, next) => {
        try {
            const result = await this.chatService.sendMessage(req.user.uid, req.body.text);
            res.status(200).json(result);
        }
        catch (err) {
            next(err);
        }
    };
    listMessages = async (req, res, next) => {
        try {
            const limit = Number(req.query.limit ?? 50);
            const cursor = req.query.cursor;
            const result = await this.chatService.list(req.user.uid, limit, cursor);
            res.status(200).json(result);
        }
        catch (err) {
            next(err);
        }
    };
}
exports.ChatController = ChatController;
