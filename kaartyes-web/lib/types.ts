export type BarcodeFormat =
  | "CODE128"
  | "CODE39"
  | "EAN13"
  | "EAN8"
  | "QR_CODE"
  | "DATAMATRIX"
  | "PDF417"
  | "AZTEC";

export interface LoyaltyCard {
  id: string;
  userId: string;
  name: string;
  cardNumber: string;
  barcodeFormat: BarcodeFormat;
  color: string;
  textColorLight?: boolean;
  isFavorite: boolean;
  nickname?: string;
  notes?: string;
  usageCount: number;
  photoFrontUrl?: string;
  photoBackUrl?: string;
  createdAt: number;
  updatedAt: number;
}

export const TILE_COLORS = [
  "#1976D2",
  "#2196F3",
  "#0D47A1",
  "#039BE5",
  "#388E3C",
  "#009688",
  "#2E7D32",
  "#66BB6A",
  "#D32F2F",
  "#E91E63",
  "#7B1FA2",
  "#9575CD",
  "#F57C00",
  "#FDD835",
  "#C2185B",
  "#757575",
  "#212121",
  "#5D4037",
];

export function isLightColor(hex: string): boolean {
  const c = hex.replace("#", "");
  const r = parseInt(c.substring(0, 2), 16) / 255;
  const g = parseInt(c.substring(2, 4), 16) / 255;
  const b = parseInt(c.substring(4, 6), 16) / 255;
  return 0.2126 * r + 0.7152 * g + 0.0722 * b > 0.4;
}

export function cardInitials(displayName: string): string {
  const words = displayName.trim().split(/\s+/).filter(Boolean);
  if (words.length >= 2) return (words[0][0] + words[1][0]).toUpperCase();
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase();
  return "?";
}

export function cardDisplayName(card: Pick<LoyaltyCard, "name" | "nickname">): string {
  return card.nickname?.trim() ? card.nickname : card.name;
}

export interface UserProfile {
  uid: string;
  email: string | null;
  displayName: string | null;
  photoURL: string | null;
}
