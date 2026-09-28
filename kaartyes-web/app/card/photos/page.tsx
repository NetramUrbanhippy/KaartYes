"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { getCard, updateCard } from "@/lib/firestore";
import { uploadCardPhoto, deleteCardPhoto, type PhotoSide } from "@/lib/storage";
import TopBar from "@/components/TopBar";
import Spinner from "@/components/Spinner";

function PhotosForm() {
  const router = useRouter();
  const params = useSearchParams();
  const cardId = params.get("id");
  const { user } = useAuth();

  const [loading, setLoading] = useState(true);
  const [frontUrl, setFrontUrl] = useState<string | undefined>();
  const [backUrl, setBackUrl] = useState<string | undefined>();
  const [busy, setBusy] = useState<PhotoSide | null>(null);
  const frontInput = useRef<HTMLInputElement>(null);
  const backInput = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!cardId) return;
    getCard(cardId).then((card) => {
      setFrontUrl(card?.photoFrontUrl);
      setBackUrl(card?.photoBackUrl);
      setLoading(false);
    });
  }, [cardId]);

  async function handleUpload(side: PhotoSide, file: File) {
    if (!cardId || !user) return;
    setBusy(side);
    try {
      const url = await uploadCardPhoto(user.uid, cardId, side, file);
      await updateCard(cardId, side === "front" ? { photoFrontUrl: url } : { photoBackUrl: url });
      if (side === "front") setFrontUrl(url);
      else setBackUrl(url);
    } finally {
      setBusy(null);
    }
  }

  async function handleRemove(side: PhotoSide) {
    if (!cardId || !user) return;
    setBusy(side);
    try {
      await deleteCardPhoto(user.uid, cardId, side);
      await updateCard(cardId, side === "front" ? { photoFrontUrl: "" } : { photoBackUrl: "" });
      if (side === "front") setFrontUrl(undefined);
      else setBackUrl(undefined);
    } finally {
      setBusy(null);
    }
  }

  if (loading) {
    return <Spinner />;
  }

  return (
    <div className="min-h-screen bg-background">
      <TopBar title="Foto's" onBack={() => router.push(`/card?id=${cardId}`)} />
      <main className="p-4 flex flex-col gap-4">
        <h2 className="text-lg font-bold text-foreground">Kaartfoto&apos;s</h2>
        <div className="flex gap-3">
          <PhotoSlot
            label="Voorkant"
            url={frontUrl}
            busy={busy === "front"}
            inputRef={frontInput}
            onPick={() => frontInput.current?.click()}
            onFile={(f) => handleUpload("front", f)}
            onRemove={() => handleRemove("front")}
          />
          <PhotoSlot
            label="Achterkant"
            url={backUrl}
            busy={busy === "back"}
            inputRef={backInput}
            onPick={() => backInput.current?.click()}
            onFile={(f) => handleUpload("back", f)}
            onRemove={() => handleRemove("back")}
          />
        </div>
      </main>
    </div>
  );
}

function PhotoSlot({
  label,
  url,
  busy,
  inputRef,
  onPick,
  onFile,
  onRemove,
}: {
  label: string;
  url?: string;
  busy: boolean;
  inputRef: React.RefObject<HTMLInputElement | null>;
  onPick: () => void;
  onFile: (file: File) => void;
  onRemove: () => void;
}) {
  return (
    <div className="flex-1 flex flex-col gap-2">
      <span className="text-xs font-medium text-secondary">{label}</span>
      <div
        onClick={!url ? onPick : undefined}
        className="relative w-full aspect-[1.58/1] rounded-xl border border-outline bg-surface-variant flex items-center justify-center overflow-hidden cursor-pointer"
      >
        <input
          ref={inputRef}
          type="file"
          accept="image/*"
          capture="environment"
          className="hidden"
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) onFile(file);
            e.target.value = "";
          }}
        />
        {busy ? (
          <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
        ) : url ? (
          <>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img src={url} alt={label} className="w-full h-full object-cover" />
            <button
              onClick={(e) => {
                e.stopPropagation();
                onRemove();
              }}
              className="absolute top-1 right-1 w-6 h-6 rounded-full bg-black/50 text-white text-xs flex items-center justify-center"
              aria-label="Verwijderen"
            >
              ✕
            </button>
          </>
        ) : (
          <span className="text-xs text-secondary">+ Foto toevoegen</span>
        )}
      </div>
    </div>
  );
}

export default function PhotosPage() {
  return (
    <Suspense>
      <PhotosForm />
    </Suspense>
  );
}
