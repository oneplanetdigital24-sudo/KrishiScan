import admin from 'firebase-admin';
import { config } from '../../common/config/env';

function normalizeStorageBucket(bucket: string): string {
  return bucket.replace(/^gs:\/\//, '');
}

function buildCredential(): admin.credential.Credential | undefined {
  if (config.firebaseServiceAccountJson) {
    try {
      const serviceAccount = JSON.parse(config.firebaseServiceAccountJson) as admin.ServiceAccount;
      return admin.credential.cert(serviceAccount);
    } catch (error) {
      throw new Error(
        `Invalid FIREBASE_SERVICE_ACCOUNT_JSON: ${
          error instanceof Error ? error.message : 'Unable to parse JSON'
        }`,
      );
    }
  }

  if (config.firebaseProjectId && config.firebaseClientEmail && config.firebasePrivateKey) {
    return admin.credential.cert({
      projectId: config.firebaseProjectId,
      clientEmail: config.firebaseClientEmail,
      privateKey: config.firebasePrivateKey.replace(/\\n/g, '\n'),
    });
  }

  if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
    return admin.credential.applicationDefault();
  }

  return undefined;
}

if (!admin.apps.length) {
  const credential = buildCredential();
  const appOptions: admin.AppOptions = {
    storageBucket: normalizeStorageBucket(config.firebaseStorageBucket),
  };

  if (credential) {
    appOptions.credential = credential;
  }

  admin.initializeApp(appOptions);
}

export const firestore = admin.firestore();
export const storage = admin.storage().bucket();
export const auth = admin.auth();
export { admin };
