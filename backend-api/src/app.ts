import express from 'express';
import multer from 'multer';
import { firebaseAuthGuard } from './common/guards/firebase-auth-guard';
import { errorMiddleware } from './common/middleware/error-middleware';
import { validateBody } from './common/validators/validate-body';
import { createScanSchema, saveFcmTokenSchema, sendChatMessageSchema, updateUserSchema } from './common/validators/schemas';
import { AuthRepository } from './modules/auth/auth.repository';
import { AuthService } from './modules/auth/auth.service';
import { AuthController } from './modules/auth/auth.controller';
import { UserRepository } from './modules/users/users.repository';
import { UserService } from './modules/users/users.service';
import { UserController } from './modules/users/users.controller';
import { ScanRepository } from './modules/scans/scans.repository';
import { ScanService } from './modules/scans/scans.service';
import { ScanController } from './modules/scans/scans.controller';
import { ChatRepository } from './modules/chat/chat.repository';
import { ChatService } from './modules/chat/chat.service';
import { ChatController } from './modules/chat/chat.controller';
import { GeminiClient } from './infra/gemini/gemini-client';
import { RedisRateLimiter } from './infra/redis/redis-rate-limiter';
import { StorageClient } from './infra/storage/storage-client';
import { UploadService } from './modules/uploads/uploads.service';
import { UploadController } from './modules/uploads/uploads.controller';

const app = express();
const upload = multer({ limits: { fileSize: 2 * 1024 * 1024 } });
app.use(express.json({ limit: '2mb' }));

app.get('/health', (_req, res) => {
  res.status(200).json({ ok: true, service: 'krishiscan-backend-api' });
});

const authController = new AuthController(new AuthService(new AuthRepository()));
const userController = new UserController(new UserService(new UserRepository()));
const geminiClient = new GeminiClient();
const rateLimiter = new RedisRateLimiter();
const storageClient = new StorageClient();
const scanController = new ScanController(new ScanService(new ScanRepository(), geminiClient, rateLimiter, storageClient));
const chatController = new ChatController(new ChatService(new ChatRepository(), geminiClient, rateLimiter));
const uploadController = new UploadController(new UploadService(storageClient));

const router = express.Router();
router.use(firebaseAuthGuard);

router.post('/auth/session', authController.postSession);
router.get('/users/me', userController.getMe);
router.patch('/users/me', validateBody(updateUserSchema), userController.patchMe);
router.post('/notifications/token', validateBody(saveFcmTokenSchema), userController.saveFcmToken);

router.post('/uploads/scan-image', upload.single('file'), uploadController.uploadScanImage);

router.post('/scans', validateBody(createScanSchema), scanController.create);
router.get('/scans', scanController.list);
router.get('/scans/:scanId', scanController.getOne);
router.delete('/scans/:scanId', scanController.deleteOne);

router.post('/chat/messages', validateBody(sendChatMessageSchema), chatController.sendMessage);
router.get('/chat/messages', chatController.listMessages);

app.use('/api/v1', router);
app.use(errorMiddleware);

export default app;
