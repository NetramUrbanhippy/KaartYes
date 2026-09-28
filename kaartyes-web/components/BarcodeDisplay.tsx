"use client";

import { useEffect, useRef, useState } from "react";
import type { BarcodeFormat } from "@/lib/types";

const BWIP_ID: Record<BarcodeFormat, string> = {
  CODE128: "code128",
  CODE39: "code39",
  EAN13: "ean13",
  EAN8: "ean8",
  QR_CODE: "qrcode",
  DATAMATRIX: "datamatrix",
  PDF417: "pdf417",
  AZTEC: "azteccode",
};

function formatCardNumber(value: string): string {
  if (value.length <= 4) return value;
  return value.match(/.{1,4}/g)?.join(" ") ?? value;
}

export default function BarcodeDisplay({
  cardNumber,
  format,
}: {
  cardNumber: string;
  format: BarcodeFormat;
}) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    let cancelled = false;
    if (!cardNumber) return;

    import("bwip-js/browser").then((bwipjs) => {
      if (cancelled || !canvasRef.current) return;
      try {
        const isQr = format === "QR_CODE";
        bwipjs.toCanvas(canvasRef.current, {
          bcid: BWIP_ID[format],
          text: cardNumber,
          scale: isQr ? 4 : 3,
          height: isQr ? 32 : 14,
          includetext: false,
          paddingwidth: 4,
          paddingheight: 4,
        });
        setError(false);
      } catch {
        if (!cancelled) setError(true);
      }
    });

    return () => {
      cancelled = true;
    };
  }, [cardNumber, format]);

  return (
    <div className="flex flex-col items-center gap-3 bg-white rounded-b-2xl px-6 py-5">
      {error ? (
        <div className="w-full h-24 flex items-center justify-center bg-surface-variant rounded-lg text-sm text-secondary">
          Kan barcode niet weergeven
        </div>
      ) : (
        <canvas
          ref={canvasRef}
          className={`max-w-full ${format === "QR_CODE" ? "h-44" : "h-24"}`}
        />
      )}
      <span className="text-sm text-secondary font-mono tracking-wide">
        {formatCardNumber(cardNumber)}
      </span>
    </div>
  );
}
