"use client";

import { useRouter } from "next/navigation";
import type { ReactNode } from "react";

export default function TopBar({
  title,
  onBack,
  actions,
}: {
  title: string;
  onBack?: () => void;
  actions?: ReactNode;
}) {
  const router = useRouter();

  return (
    <header className="flex items-center justify-between gap-2 bg-background px-2 pt-safe-top pb-2 sticky top-0 z-10">
      <button
        onClick={onBack ?? (() => router.back())}
        aria-label="Terug"
        className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-surface-variant active:bg-outline transition-colors text-foreground shrink-0"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M15 18l-6-6 6-6" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </button>
      <h1 className="flex-1 text-base font-semibold text-foreground truncate text-center">
        {title}
      </h1>
      <div className="w-10 h-10 flex items-center justify-center shrink-0">{actions}</div>
    </header>
  );
}
