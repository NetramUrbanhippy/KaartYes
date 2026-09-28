"use client";

import { useEffect, useRef, useState, type ReactNode } from "react";

export default function DropdownMenu({
  trigger,
  children,
}: {
  trigger: (toggle: () => void) => ReactNode;
  children: (close: () => void) => ReactNode;
}) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    function handleClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, [open]);

  return (
    <div className="relative" ref={ref}>
      {trigger(() => setOpen((v) => !v))}
      {open && (
        <div className="absolute right-0 mt-1 min-w-[190px] bg-surface rounded-xl shadow-lg py-1 z-20">
          {children(() => setOpen(false))}
        </div>
      )}
    </div>
  );
}

export function DropdownMenuItem({
  label,
  onClick,
  checked,
}: {
  label: string;
  onClick: () => void;
  checked?: boolean;
}) {
  return (
    <button
      onClick={onClick}
      className="w-full flex items-center justify-between gap-4 px-4 py-2.5 text-sm text-foreground hover:bg-background transition-colors text-left"
    >
      {label}
      {checked && <span className="text-xs">✓</span>}
    </button>
  );
}
