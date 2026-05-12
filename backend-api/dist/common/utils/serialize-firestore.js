"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.serializeFirestoreValue = serializeFirestoreValue;
exports.serializeFirestoreDoc = serializeFirestoreDoc;
function isPlainObject(value) {
    return typeof value === 'object' && value !== null && !Array.isArray(value);
}
function isFirestoreTimestamp(value) {
    return isPlainObject(value) && typeof value.toDate === 'function';
}
function serializeFirestoreValue(value) {
    if (value == null)
        return value;
    if (value instanceof Date)
        return value.toISOString();
    if (isFirestoreTimestamp(value))
        return value.toDate().toISOString();
    if (Array.isArray(value))
        return value.map(serializeFirestoreValue);
    if (isPlainObject(value)) {
        return Object.fromEntries(Object.entries(value).map(([key, item]) => [key, serializeFirestoreValue(item)]));
    }
    return value;
}
function serializeFirestoreDoc(doc) {
    return serializeFirestoreValue(doc);
}
