"use client";

import { cardDisplayName, cardInitials, isLightColor, type LoyaltyCard } from "@/lib/types";

export default function CardTile({
  card,
  onClick,
}: {
  card: LoyaltyCard;
  onClick: () => void;
}) {
  const displayName = cardDisplayName(card);
  const useLightText = card.textColorLight ?? !isLightColor(card.color);
  const textColor = useLightText ? "#FFFFFF" : "#1A1A1A";
  const pillBg = useLightText ? "rgba(255,255,255,0.22)" : "rgba(0,0,0,0.12)";

  return (
    <button
      onClick={onClick}
      style={{ backgroundColor: card.color }}
      className="relative aspect-[1.6/1] rounded-2xl shadow-sm active:scale-95 transition-transform flex items-center justify-center p-3"
    >
      {card.isFavorite && (
        <span className="absolute top-1.5 left-1.5 text-base leading-none" aria-hidden>
          ⭐
        </span>
      )}
      <div className="flex flex-col items-center gap-1.5">
        <span className="text-2xl font-bold" style={{ color: textColor }}>
          {cardInitials(displayName)}
        </span>
        {displayName && (
          <span
            className="text-xs font-medium px-2.5 py-0.5 rounded-full truncate max-w-[9rem]"
            style={{ color: textColor, backgroundColor: pillBg }}
          >
            {displayName}
          </span>
        )}
      </div>
    </button>
  );
}
