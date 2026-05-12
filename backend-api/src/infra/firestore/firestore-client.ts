import admin from 'firebase-admin';
import { config } from '../../common/config/env';

if (!admin.apps.length) {
  admin.initializeApp({
    storageBucket: config.firebaseStorageBucket,
  });
}

export const firestore = admin.firestore();
export const storage = admin.storage().bucket();
export const auth = admin.auth();
export { admin };
