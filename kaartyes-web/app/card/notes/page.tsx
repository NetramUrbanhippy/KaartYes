"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { getCard, updateCard } from "@/lib/firestore";
import TopBar from "@/components/TopBar";
import Spinner from "@/components/Spinner";

function NotesForm() {
  const router = useRouter();
  const params = useSearchParams();
  const cardId = params.get("id");

  const [loading, setLoading] = useState(true);
  const [note, setNote] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!cardId) return;
    getCard(cardId).then((card) => {
      setNote(card?.notes ?? "");
      setLoading(false);
    });
  }, [cardId]);

  async function handleSave() {
    if (!cardId) return;
    setSaving(true);
    try {
      await updateCard(cardId, { notes: note });
      router.replace(`/card?id=${cardId}`);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return <Spinner />;
  }

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <TopBar title="Notitie toevoegen" onBack={() => router.push(`/card?id=${cardId}`)} />
      <main className="flex-1 p-4">
        <textarea
          value={note}
          onChange={(e) => setNote(e.target.value)}
          placeholder="Begin met schrijven…"
          className="w-full h-full min-h-64 resize-none outline-none text-foreground bg-transparent"
        />
      </main>
      <div className="p-4 pb-safe-bottom">
        <button
          onClick={handleSave}
          disabled={saving}
          className={`w-full rounded-full py-3.5 font-semibold transition-opacity disabled:opacity-60 ${
            note.trim() ? "bg-primary text-on-primary" : "bg-surface-variant text-secondary"
          }`}
        >
          {saving ? "Bezig..." : "Opslaan"}
        </button>
      </div>
    </div>
  );
}

export default function NotesPage() {
  return (
    <Suspense>
      <NotesForm />
    </Suspense>
  );
}
