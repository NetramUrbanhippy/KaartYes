import { ref, uploadBytes, getDownloadURL, deleteObject } from "firebase/storage";
import { getFirebaseStorage } from "./firebase";

export type PhotoSide = "front" | "back";

// Path is scoped under the owner's uid so storage.rules can enforce
// per-user isolation the same way firestore.rules does for card documents.
function photoPath(userId: string, cardId: string, side: PhotoSide) {
  return `users/${userId}/cards/${cardId}/${side}.jpg`;
}

export async function uploadCardPhoto(
  userId: string,
  cardId: string,
  side: PhotoSide,
  file: File | Blob
): Promise<string> {
  const storageRef = ref(getFirebaseStorage(), photoPath(userId, cardId, side));
  await uploadBytes(storageRef, file, { contentType: "image/jpeg" });
  return getDownloadURL(storageRef);
}

export async function deleteCardPhoto(
  userId: string,
  cardId: string,
  side: PhotoSide
): Promise<void> {
  const storageRef = ref(getFirebaseStorage(), photoPath(userId, cardId, side));
  await deleteObject(storageRef).catch(() => undefined);
}
