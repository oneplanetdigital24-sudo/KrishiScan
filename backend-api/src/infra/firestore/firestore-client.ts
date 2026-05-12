import admin from 'firebase-admin';
import { config } from '../../common/config/env';

function normalizeStorageBucket(bucket: string): string {
  return bucket.replace(/^gs:\/\//, '');
}

function buildCredential(): admin.credential.Credential | undefined {
  if (config.firebaseServiceAccountJson) {
    const serviceAccount = JSON.parse(config.firebaseServiceAccountJson) as admin.ServiceAccount;
    return admin.credential.cert(serviceAccount);
  }

  if (config.firebaseProjectId && config.firebaseClientEmail && config.firebasePrivateKey) {
    return admin.credential.cert({
      projectId: config.firebaseProjectId,
      clientEmail: config.firebaseClientEmail,
      privateKey: config.firebasePrivateKey.replace(/\\n/g, '\n'),
    });
  }

  return undefined;
}

if (!admin.apps.length) {
  const credential = buildCredential();
  admin.initializeApp({
    credential,
    storageBucket: normalizeStorageBucket(config.firebaseStorageBucket),
  });
}

export const firestore = admin.firestore();
export const storage = admin.storage().bucket();
export const auth = admin.auth();
export { admin };
