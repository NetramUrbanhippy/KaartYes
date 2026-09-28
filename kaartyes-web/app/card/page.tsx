"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import {
  subscribeToCard,
  incrementUsage,
  setFavorite,
  getFavoriteCount,
  deleteCard,
  MAX_FAVORITES,
} from "@/lib/firestore";
import { cardDisplayName, isLightColor, type LoyaltyCard } from "@/lib/types";
import BarcodeDisplay from "@/components/BarcodeDisplay";
import TopBar from "@/components/TopBar";
import Spinner from "@/components/Spinner";

function CardDetail() {
  const router = useRouter();
  const params = useSearchParams();
  const cardId = params.get("id");
  const { user } = useAuth();

  const [card, setCard] = useState<LoyaltyCard | null | undefined>(undefined);
  const [showQr, setShowQr] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [favoriteLimitMessage, setFavoriteLimitMessage] = useState(false);
  const usageRecorded = useRef<string | null>(null);

  useEffect(() => {
    if (!cardId) return;
    const unsubscribe = subscribeToCard(cardId, (loaded) => {
      setCard(loaded);
      if (loaded && usageRecorded.current !== cardId) {
        usageRecorded.current = cardId;
        setShowQr(loaded.barcodeFormat === "QR_CODE");
        incrementUsage(cardId, loaded.usageCount);
      }
    });
    return unsubscribe;
  }, [cardId]);

  if (!cardId) {
    router.replace("/home");
    return null;
  }

  if (card === undefined) {
    return <Spinner />;
  }

  if (card === null) {
    return (
      <div className="min-h-screen bg-background">
        <TopBar title="Kaart" onBack={() => router.push("/home")} />
        <p className="p-6 text-center text-secondary">Deze kaart bestaat niet meer.</p>
      </div>
    );
  }

  const displayName = cardDisplayName(card);
  const useLightText = card.textColorLight ?? !isLightColor(card.color);
  const onTileColor = useLightText ? "#FFFFFF" : "#1A1A1A";
  const canToggleFormat = card.barcodeFormat === "QR_CODE" || card.barcodeFormat === "CODE128";

  async function toggleFavorite() {
    if (!card || !user) return;
    if (!card.isFavorite) {
      const count = await getFavoriteCount(user.uid);
      if (count >= MAX_FAVORITES) {
        setFavoriteLimitMessage(true);
        return;
      }
    }
    await setFavorite(card.id, !card.isFavorite);
  }

  async function handleDelete() {
    if (!card) return;
    setShowDeleteDialog(false);
    await deleteCard(card.id);
    router.replace("/home");
  }

  return (
    <div className="min-h-screen bg-background pb-10">
      <TopBar
        title=""
        onBack={() => router.push("/home")}
        actions={
          <button onClick={toggleFavorite} aria-label="Favoriet" className="text-xl">
            {card.isFavorite ? "⭐" : "☆"}
          </button>
        }
      />

      <main className="px-4 flex flex-col gap-2">
        <div className="rounded-2xl overflow-hidden shadow-sm">
          <div
            className="flex items-center justify-between px-4 py-3.5"
            style={{ backgroundColor: card.color }}
          >
            <span className="font-semibold" style={{ color: onTileColor }}>
              {displayName}
            </span>
            <button
              onClick={() => router.push(`/card/edit?id=${card.id}`)}
              className="text-xs font-medium px-3 py-1 rounded-full"
              style={{
                color: onTileColor,
                backgroundColor: useLightText ? "rgba(255,255,255,0.2)" : "rgba(0,0,0,0.12)",
              }}
            >
              Details
            </button>
          </div>
          <BarcodeDisplay
            cardNumber={card.cardNumber}
            format={showQr ? "QR_CODE" : card.barcodeFormat}
          />
        </div>

        {canToggleFormat && (
          <button
            onClick={() => setShowQr((v) => !v)}
            className="self-center text-sm text-secondary py-2"
          >
            {showQr ? "Barcode" : "QR-code"}
          </button>
        )}

        <h2 className="text-lg font-bold text-foreground mt-4 mb-1">Beheren</h2>
        <div className="bg-surface rounded-2xl shadow-sm overflow-hidden">
          <ManageItem label="Kaart bewerken" onClick={() => router.push(`/card/edit?id=${card.id}`)} />
          <ManageItem label="Notities" onClick={() => router.push(`/card/notes?id=${card.id}`)} />
          <ManageItem
            label="Kaart verwijderen"
            danger
            last
            onClick={() => setShowDeleteDialog(true)}
          />
        </div>
      </main>

      {showDeleteDialog && (
        <ConfirmDialog
          title="Kaart verwijderen"
          message="Weet je zeker dat je deze kaart wilt verwijderen?"
          confirmLabel="Verwijderen"
          danger
          onConfirm={handleDelete}
          onCancel={() => setShowDeleteDialog(false)}
        />
      )}

      {favoriteLimitMessage && (
        <ConfirmDialog
          title="Favoriet"
          message="Maximaal 4 favorieten. Verwijder eerst een ster."
          confirmLabel="Annuleren"
          onConfirm={() => setFavoriteLimitMessage(false)}
          onCancel={() => setFavoriteLimitMessage(false)}
          hideCancel
        />
      )}
    </div>
  );
}

function ManageItem({
  label,
  onClick,
  danger,
  last,
}: {
  label: string;
  onClick: () => void;
  danger?: boolean;
  last?: boolean;
}) {
  return (
    <button
      onClick={onClick}
      className={`w-full flex items-center justify-between px-4 py-4 hover:bg-background active:bg-surface-variant transition-colors ${
        last ? "" : "border-b border-outline"
      }`}
    >
      <span className={`font-medium ${danger ? "text-error" : "text-foreground"}`}>{label}</span>
      <span className="text-secondary">›</span>
    </button>
  );
}

function ConfirmDialog({
  title,
  message,
  confirmLabel,
  danger,
  hideCancel,
  onConfirm,
  onCancel,
}: {
  title: string;
  message: string;
  confirmLabel: string;
  danger?: boolean;
  hideCancel?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center px-6 z-20">
      <div className="bg-surface rounded-2xl p-5 w-full max-w-sm flex flex-col gap-3">
        <h3 className="font-bold text-foreground">{title}</h3>
        <p className="text-sm text-secondary">{message}</p>
        <div className="flex justify-end gap-4 mt-2">
          {!hideCancel && (
            <button onClick={onCancel} className="text-sm font-medium text-secondary px-2 py-1">
              Annuleren
            </button>
          )}
          <button
            onClick={onConfirm}
            className={`text-sm font-semibold px-2 py-1 ${danger ? "text-error" : "text-primary"}`}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function CardDetailPage() {
  return (
    <Suspense>
      <CardDetail />
    </Suspense>
  );
}
