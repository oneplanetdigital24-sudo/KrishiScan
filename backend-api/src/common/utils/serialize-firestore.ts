function isPlainObject(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function isFirestoreTimestamp(value: unknown): value is { toDate: () => Date } {
  return isPlainObject(value) && typeof value.toDate === 'function';
}

export function serializeFirestoreValue(value: unknown): unknown {
  if (value == null) return value;
  if (value instanceof Date) return value.toISOString();
  if (isFirestoreTimestamp(value)) return value.toDate().toISOString();
  if (Array.isArray(value)) return value.map(serializeFirestoreValue);

  if (isPlainObject(value)) {
    return Object.fromEntries(
      Object.entries(value).map(([key, item]) => [key, serializeFirestoreValue(item)]),
    );
  }

  return value;
}

export function serializeFirestoreDoc(doc: Record<string, unknown>): Record<string, unknown> {
  return serializeFirestoreValue(doc) as Record<string, unknown>;
}
