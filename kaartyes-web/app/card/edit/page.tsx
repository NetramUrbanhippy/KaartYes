"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { getCard, updateCard } from "@/lib/firestore";
import { TILE_COLORS, cardInitials, isLightColor } from "@/lib/types";
import TopBar from "@/components/TopBar";
import Spinner from "@/components/Spinner";

function EditCardForm() {
  const router = useRouter();
  const params = useSearchParams();
  const cardId = params.get("id");

  const [loading, setLoading] = useState(true);
  const [name, setName] = useState("");
  const [cardNumber, setCardNumber] = useState("");
  const [color, setColor] = useState("#1976D2");
  const [textLight, setTextLight] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!cardId) return;
    getCard(cardId).then((card) => {
      if (card) {
        setName(card.name);
        setCardNumber(card.cardNumber);
        setColor(card.color);
        setTextLight(card.textColorLight ?? !isLightColor(card.color));
      }
      setLoading(false);
    });
  }, [cardId]);

  async function handleSave() {
    if (!cardId || !name.trim() || !cardNumber.trim()) return;
    setSaving(true);
    try {
      await updateCard(cardId, {
        name: name.trim(),
        cardNumber: cardNumber.trim(),
        color,
        textColorLight: textLight,
      });
      router.replace(`/card?id=${cardId}`);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <Spinner />;
  }

  const onTileColor = textLight ? "#FFFFFF" : "#1A1A1A";

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <TopBar title="Kaart bewerken" onBack={() => router.push(`/card?id=${cardId}`)} />
      <main className="flex-1 p-4 flex flex-col gap-5">
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-secondary">Winkelnaam</label>
          <input
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full rounded-xl border border-outline px-4 py-3 text-foreground bg-surface outline-none focus:border-primary"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-secondary">Kaartnummer</label>
          <input
            value={cardNumber}
            onChange={(e) => setCardNumber(e.target.value)}
            inputMode="numeric"
            className="w-full rounded-xl border border-outline px-4 py-3 text-foreground bg-surface outline-none focus:border-primary"
          />
        </div>

        <div className="flex flex-col gap-2">
          <label className="text-sm font-medium text-foreground">Kaartkleur</label>
          <div className="flex flex-wrap gap-2.5">
            {TILE_COLORS.map((c) => (
              <button
                key={c}
                onClick={() => setColor(c)}
                style={{ backgroundColor: c }}
                className={`w-10 h-10 rounded-full flex items-center justify-center transition-transform ${
                  color === c ? "ring-2 ring-offset-2 ring-primary scale-105" : ""
                }`}
                aria-label={c}
              >
                {color === c && <span className="text-white text-sm">✓</span>}
              </button>
            ))}
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <label className="text-sm font-medium text-foreground">Tekst kleur</label>
          <div className="flex items-center gap-4">
            <div
              style={{ backgroundColor: color }}
              className="w-20 h-12 rounded-lg flex items-center justify-center shrink-0"
            >
              <span style={{ color: onTileColor }} className="font-bold text-sm">
                {cardInitials(name || "AB")}
              </span>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setTextLight(false)}
                className={`px-3 py-1.5 rounded-full text-sm border ${
                  !textLight
                    ? "bg-primary text-on-primary border-primary"
                    : "border-outline text-foreground"
                }`}
              >
                Donker
              </button>
              <button
                onClick={() => setTextLight(true)}
                className={`px-3 py-1.5 rounded-full text-sm border ${
                  textLight
                    ? "bg-primary text-on-primary border-primary"
                    : "border-outline text-foreground"
                }`}
              >
                Licht
              </button>
            </div>
          </div>
        </div>
      </main>
      <div className="p-4 pb-safe-bottom">
        <button
          onClick={handleSave}
          disabled={saving || !name.trim() || !cardNumber.trim()}
          className="w-full bg-primary text-on-primary rounded-full py-3.5 font-semibold active:opacity-90 transition-opacity disabled:opacity-60"
        >
          {saving ? "Bezig..." : "Opslaan"}
        </button>
      </div>
    </div>
  );
}

export default function EditCardPage() {
  return (
    <Suspense>
      <EditCardForm />
    </Suspense>
  );
}
