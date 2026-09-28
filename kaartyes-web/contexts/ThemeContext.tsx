"use client";

import { useEffect, type ReactNode } from "react";

const DARK_QUERY = "(prefers-color-scheme: dark)";

function applyTheme(dark: boolean) {
  document.documentElement.classList.toggle("dark", dark);
  const meta = document.querySelector('meta[name="theme-color"]');
  if (meta) meta.setAttribute("content", dark ? "#121212" : "#f2f2f2");
}

// Het thema volgt altijd de systeeminstelling. Geeft het systeem geen
// voorkeur door (of ondersteunt de browser matchMedia niet), dan blijft het
// licht. De inline script in layout.tsx zet de eerste waarde vóór hydration;
// hier luisteren we naar wijzigingen terwijl de app open staat.
export function ThemeProvider({ children }: { children: ReactNode }) {
  useEffect(() => {
    if (typeof window.matchMedia !== "function") {
      applyTheme(false);
      return;
    }
    const media = window.matchMedia(DARK_QUERY);
    applyTheme(media.matches);
    const onChange = (e: MediaQueryListEvent) => applyTheme(e.matches);
    media.addEventListener("change", onChange);
    return () => media.removeEventListener("change", onChange);
  }, []);

  return <>{children}</>;
}
