import {
  collection,
  doc,
  getDoc,
  getCountFromServer,
  addDoc,
  updateDoc,
  deleteDoc,
  query,
  where,
  orderBy,
  onSnapshot,
  type Unsubscribe,
} from "firebase/firestore";
import { getFirebaseDb } from "./firebase";
import type { LoyaltyCard } from "./types";

const CARDS_COLLECTION = "cards";

export function subscribeToUserCards(
  userId: string,
  onChange: (cards: LoyaltyCard[]) => void,
  onError?: (error: Error) => void
): Unsubscribe {
  const q = query(
    collection(getFirebaseDb(), CARDS_COLLECTION),
    where("userId", "==", userId),
    orderBy("updatedAt", "desc")
  );
  return onSnapshot(
    q,
    (snapshot) => {
      onChange(snapshot.docs.map((d) => ({ id: d.id, ...d.data() } as LoyaltyCard)));
    },
    (error) => onError?.(error)
  );
}

export async function getCard(cardId: string): Promise<LoyaltyCard | null> {
  const ref = doc(getFirebaseDb(), CARDS_COLLECTION, cardId);
  const snap = await getDoc(ref);
  if (!snap.exists()) return null;
  return { id: snap.id, ...snap.data() } as LoyaltyCard;
}

export function subscribeToCard(
  cardId: string,
  onChange: (card: LoyaltyCard | null) => void
): Unsubscribe {
  const ref = doc(getFirebaseDb(), CARDS_COLLECTION, cardId);
  return onSnapshot(ref, (snap) => {
    onChange(snap.exists() ? ({ id: snap.id, ...snap.data() } as LoyaltyCard) : null);
  });
}

export async function addCard(
  data: Omit<LoyaltyCard, "id" | "createdAt" | "updatedAt" | "usageCount" | "isFavorite">
): Promise<string> {
  const now = Date.now();
  const ref = await addDoc(collection(getFirebaseDb(), CARDS_COLLECTION), {
    ...data,
    usageCount: 0,
    isFavorite: false,
    createdAt: now,
    updatedAt: now,
  });
  return ref.id;
}

export async function updateCard(
  cardId: string,
  data: Partial<Omit<LoyaltyCard, "id" | "userId" | "createdAt">>
): Promise<void> {
  const ref = doc(getFirebaseDb(), CARDS_COLLECTION, cardId);
  await updateDoc(ref, { ...data, updatedAt: Date.now() });
}

export async function deleteCard(cardId: string): Promise<void> {
  await deleteDoc(doc(getFirebaseDb(), CARDS_COLLECTION, cardId));
}

export async function incrementUsage(cardId: string, current: number): Promise<void> {
  const ref = doc(getFirebaseDb(), CARDS_COLLECTION, cardId);
  await updateDoc(ref, { usageCount: current + 1, updatedAt: Date.now() });
}

export async function setFavorite(cardId: string, isFavorite: boolean): Promise<void> {
  const ref = doc(getFirebaseDb(), CARDS_COLLECTION, cardId);
  await updateDoc(ref, { isFavorite, updatedAt: Date.now() });
}

export const MAX_FAVORITES = 4;

export async function getFavoriteCount(userId: string): Promise<number> {
  const q = query(
    collection(getFirebaseDb(), CARDS_COLLECTION),
    where("userId", "==", userId),
    where("isFavorite", "==", true)
  );
  const snapshot = await getCountFromServer(q);
  return snapshot.data().count;
}
