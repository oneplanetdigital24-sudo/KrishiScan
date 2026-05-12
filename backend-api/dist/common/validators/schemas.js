"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.saveFcmTokenSchema = exports.sendChatMessageSchema = exports.createScanSchema = exports.updateUserSchema = void 0;
const zod_1 = require("zod");
exports.updateUserSchema = zod_1.z.object({
    name: zod_1.z.string().min(3).max(100).optional(),
    phone: zod_1.z.string().regex(/^[6-9][0-9]{9}$/).nullable().optional(),
    state: zod_1.z.string().min(2).max(64).optional(),
    language: zod_1.z.enum(['en', 'hi', 'as']).optional(),
    role: zod_1.z.enum(['farmer', 'student', 'extension_worker']).optional(),
}).refine((v) => Object.keys(v).length > 0, { message: 'No fields to update' });
exports.createScanSchema = zod_1.z.object({
    cropName: zod_1.z.string().min(1).max(100),
    diseaseName: zod_1.z.string().min(1).max(100),
    confidence: zod_1.z.number().min(0).max(1),
    imageUrl: zod_1.z.string().url(),
    imagePath: zod_1.z.string().min(3).optional(),
    location: zod_1.z.object({ lat: zod_1.z.number(), lng: zod_1.z.number() }).nullable().optional(),
});
exports.sendChatMessageSchema = zod_1.z.object({
    text: zod_1.z.string().trim().min(1).max(500),
});
exports.saveFcmTokenSchema = zod_1.z.object({
    fcmToken: zod_1.z.string().min(10).max(4096),
});
