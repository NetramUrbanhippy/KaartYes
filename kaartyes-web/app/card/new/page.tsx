"use client";

import { useRouter } from "next/navigation";
import TopBar from "@/components/TopBar";

export default function AddCardPage() {
  const router = useRouter();

  return (
    <div className="min-h-screen bg-background">
      <TopBar title="Kaart toevoegen" onBack={() => router.push("/home")} />
      <main className="p-4">
        <div className="bg-surface rounded-2xl shadow-sm overflow-hidden">
          <button
            onClick={() => router.push("/card/new/scan")}
            className="w-full flex items-center gap-4 px-4 py-4 hover:bg-background active:bg-surface-variant transition-colors border-b border-outline"
          >
            <div className="w-11 h-11 rounded-xl bg-surface-variant flex items-center justify-center text-xl shrink-0">
              📷
            </div>
            <span className="flex-1 text-left font-medium text-foreground">
              Barcode scannen
            </span>
            <span className="text-secondary">›</span>
          </button>
          <button
            onClick={() => router.push("/card/new/manual")}
            className="w-full flex items-center gap-4 px-4 py-4 hover:bg-background active:bg-surface-variant transition-colors"
          >
            <div className="w-11 h-11 rounded-xl bg-surface-variant flex items-center justify-center text-xl shrink-0">
              ✏️
            </div>
            <span className="flex-1 text-left font-medium text-foreground">
              Kaartnummer handmatig invoeren
            </span>
            <span className="text-secondary">›</span>
          </button>
        </div>
      </main>
    </div>
  );
}
