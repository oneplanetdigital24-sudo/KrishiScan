import { z } from 'zod';

export const updateUserSchema = z.object({
  name: z.string().min(3).max(100).optional(),
  phone: z.string().regex(/^[6-9][0-9]{9}$/).nullable().optional(),
  state: z.string().min(2).max(64).optional(),
  language: z.enum(['en', 'hi', 'as']).optional(),
  role: z.enum(['farmer', 'student', 'extension_worker']).optional(),
}).refine((v) => Object.keys(v).length > 0, { message: 'No fields to update' });

export const createScanSchema = z.object({
  cropName: z.string().min(1).max(100),
  diseaseName: z.string().min(1).max(100),
  confidence: z.number().min(0).max(1),
  imageUrl: z.string().url(),
  imagePath: z.string().min(3).optional(),
  location: z.object({ lat: z.number(), lng: z.number() }).nullable().optional(),
});

export const sendChatMessageSchema = z.object({
  text: z.string().trim().min(1).max(500),
});

export const saveFcmTokenSchema = z.object({
  fcmToken: z.string().min(10).max(4096),
});
