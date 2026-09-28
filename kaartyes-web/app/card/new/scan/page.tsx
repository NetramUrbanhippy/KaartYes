"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import TopBar from "@/components/TopBar";
import type { BarcodeFormat } from "@/lib/types";

const READER_ID = "barcode-reader";

const FORMAT_BY_CODE: Record<number, BarcodeFormat> = {
  0: "QR_CODE",
  1: "AZTEC",
  3: "CODE39",
  5: "CODE128",
  6: "DATAMATRIX",
  9: "EAN13",
  10: "EAN8",
  11: "PDF417",
};

// Fully releases the camera before we navigate away: html5-qrcode's own
// stop() sometimes leaves the MediaStream referenced, which can make the
// browser abort the following navigation on some Android/Chrome builds.
async function releaseCamera(scanner: import("html5-qrcode").Html5Qrcode) {
  await scanner.stop().catch(() => undefined);
  document.querySelectorAll<HTMLVideoElement>(`#${READER_ID} video`).forEach((video) => {
    const stream = video.srcObject as MediaStream | null;
    stream?.getTracks().forEach((track) => track.stop());
    video.srcObject = null;
  });
  scanner.clear();
}

export default function ScanPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [detected, setDetected] = useState(false);
  const stoppedRef = useRef(false);

  useEffect(() => {
    stoppedRef.current = false;
    let scanner: import("html5-qrcode").Html5Qrcode | null = null;

    import("html5-qrcode").then(async ({ Html5Qrcode }) => {
      if (stoppedRef.current) return;
      scanner = new Html5Qrcode(READER_ID, { verbose: false });
      try {
        await scanner.start(
          { facingMode: "environment" },
          // Geen qrbox: html5-qrcode tekent dan zijn eigen kader + donkere
          // schaduw over de video, die door object-cover niet samenvalt met ons
          // eigen kader (half zichtbaar, gespiegelde hoeken). We scannen het
          // hele beeld en tonen alleen onze eigen overlay.
          { fps: 10 },
          async (decodedText, decodedResult) => {
            if (stoppedRef.current || !scanner) return;
            stoppedRef.current = true;
            setDetected(true);
            const formatCode = decodedResult.result.format?.format;
            const format = FORMAT_BY_CODE[formatCode as number] ?? "CODE128";
            const target = `/card/new/manual?code=${encodeURIComponent(decodedText)}&format=${format}`;

            // Matches the Android app's short "Kaart gevonden!" pause before navigating.
            await new Promise((resolve) => setTimeout(resolve, 400));
            await releaseCamera(scanner);

            router.replace(target);
            // Safety net: if the client-side transition silently gets stuck
            // (e.g. a stale service-worker cache from a recent deploy), force
            // a real navigation instead of leaving the user on a dead page.
            setTimeout(() => {
              if (window.location.pathname === "/card/new/scan") {
                window.location.href = target;
              }
            }, 1500);
          },
          undefined
        );
      } catch {
        setError(
          "Camera niet beschikbaar. Controleer of je toestemming hebt gegeven, of voer de kaart handmatig in."
        );
      }
    });

    return () => {
      if (stoppedRef.current) return;
      stoppedRef.current = true;
      scanner?.stop().catch(() => undefined);
    };
  }, [router]);

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <TopBar title="Barcode scannen" onBack={() => router.push("/card/new")} />
      <main className="flex-1 p-4 flex flex-col gap-4">
        <div className="flex-1 rounded-2xl overflow-hidden bg-black relative min-h-80">
          <div id={READER_ID} className="absolute inset-0 [&_video]:object-cover [&_video]:!w-full [&_video]:!h-full [&_#qr-shaded-region]:!hidden" />

          {!error && (
            <div className="absolute inset-0 flex flex-col items-center justify-center gap-4">
              <div className="relative w-[260px] h-[140px] rounded-sm shadow-[0_0_0_9999px_rgba(0,0,0,0.35)]">
                {!detected && (
                  <div className="absolute inset-0 overflow-hidden rounded-sm">
                    <div className="absolute left-0 right-0 h-[2px] bg-gradient-to-r from-transparent via-[#2196F3] to-transparent animate-scan-line" />
                  </div>
                )}
                <ScanCorners detected={detected} />
              </div>

              <p
                className={`text-xs px-3 py-1 rounded-full ${
                  detected ? "text-[#4CAF50] bg-black/40" : "text-white/80 bg-black/30"
                }`}
              >
                {detected ? "Kaart gevonden!" : "Richt je camera op de barcode of QR-code"}
              </p>
            </div>
          )}
        </div>
        {error && <p className="text-sm text-error text-center">{error}</p>}
        <button
          onClick={() => router.push("/card/new/manual")}
          className="w-full flex items-center gap-4 bg-surface rounded-2xl px-4 py-4 shadow-sm hover:bg-surface-variant transition-colors"
        >
          <div className="w-11 h-11 rounded-xl bg-surface-variant flex items-center justify-center text-xl shrink-0">
            ✏️
          </div>
          <span className="flex-1 text-left font-medium text-foreground">
            Kaartnummer handmatig invoeren
          </span>
          <span className="text-secondary">›</span>
        </button>
      </main>
    </div>
  );
}

function ScanCorners({ detected }: { detected: boolean }) {
  const color = detected ? "#4CAF50" : "white";
  const pulseClass = detected ? "" : "animate-corner-pulse";
  const corners = [
    { position: "top-0 left-0", isTop: true, isLeft: true },
    { position: "top-0 right-0", isTop: true, isLeft: false },
    { position: "bottom-0 left-0", isTop: false, isLeft: true },
    { position: "bottom-0 right-0", isTop: false, isLeft: false },
  ] as const;

  return (
    <>
      {corners.map(({ position, isTop, isLeft }) => {
        return (
          <div key={position} className={`absolute w-6 h-6 ${position} ${pulseClass}`}>
            <div
              className="absolute h-[3px] w-6"
              style={{
                backgroundColor: color,
                top: isTop ? 0 : "auto",
                bottom: isTop ? "auto" : 0,
                left: isLeft ? 0 : "auto",
                right: isLeft ? "auto" : 0,
              }}
            />
            <div
              className="absolute w-[3px] h-6"
              style={{
                backgroundColor: color,
                top: isTop ? 0 : "auto",
                bottom: isTop ? "auto" : 0,
                left: isLeft ? 0 : "auto",
                right: isLeft ? "auto" : 0,
              }}
            />
          </div>
        );
      })}
    </>
  );
}
