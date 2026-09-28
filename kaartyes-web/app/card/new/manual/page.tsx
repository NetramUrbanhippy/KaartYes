"use client";

import { Suspense, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { addCard } from "@/lib/firestore";
import { TILE_COLORS, type BarcodeFormat } from "@/lib/types";
import TopBar from "@/components/TopBar";

function ManualEntryForm() {
  const router = useRouter();
  const params = useSearchParams();
  const { user } = useAuth();

  const prefilledCode = params.get("code") ?? "";
  const format = (params.get("format") as BarcodeFormat | null) ?? "CODE128";

  const [name, setName] = useState("");
  const [cardNumber, setCardNumber] = useState(prefilledCode);
  const [nameError, setNameError] = useState(false);
  const [numberError, setNumberError] = useState(false);
  const [saving, setSaving] = useState(false);

  async function handleSubmit() {
    const nameBlank = !name.trim();
    const numberBlank = !cardNumber.trim();
    setNameError(nameBlank);
    setNumberError(numberBlank);
    if (nameBlank || numberBlank || !user) return;

    setSaving(true);
    try {
      const color = TILE_COLORS[Math.floor(Math.random() * TILE_COLORS.length)];
      await addCard({
        userId: user.uid,
        name: name.trim(),
        cardNumber: cardNumber.trim(),
        barcodeFormat: format,
        color,
      });
      router.replace("/home");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <TopBar title="Andere kaart" onBack={() => router.push("/card/new")} />
      <main className="flex-1 p-4 flex flex-col gap-4">
        <h2 className="text-xl font-bold text-foreground">Kaartnummer handmatig toevoegen</h2>
        <p className="text-sm text-secondary">Voer het nummer in dat op je kaart staat</p>

        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-secondary">Kaartnummer</label>
          <input
            value={cardNumber}
            onChange={(e) => {
              setCardNumber(e.target.value);
              setNumberError(false);
            }}
            inputMode="numeric"
            className={`w-full rounded-xl border px-4 py-3 text-foreground bg-surface outline-none focus:border-primary ${
              numberError ? "border-error" : "border-outline"
            }`}
          />
          {numberError && <span className="text-xs text-error">Kaartnummer is verplicht</span>}
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-secondary">Winkelnaam</label>
          <input
            value={name}
            onChange={(e) => {
              setName(e.target.value);
              setNameError(false);
            }}
            className={`w-full rounded-xl border px-4 py-3 text-foreground bg-surface outline-none focus:border-primary ${
              nameError ? "border-error" : "border-outline"
            }`}
          />
          {nameError && <span className="text-xs text-error">Winkelnaam is verplicht</span>}
        </div>
      </main>
      <div className="p-4 pb-safe-bottom">
        <button
          onClick={handleSubmit}
          disabled={saving}
          className="w-full bg-primary text-on-primary rounded-full py-3.5 font-semibold active:opacity-90 transition-opacity disabled:opacity-60"
        >
          {saving ? "Bezig..." : "Toevoegen"}
        </button>
      </div>
    </div>
  );
}

export default function ManualEntryPage() {
  return (
    <Suspense>
      <ManualEntryForm />
    </Suspense>
  );
}
