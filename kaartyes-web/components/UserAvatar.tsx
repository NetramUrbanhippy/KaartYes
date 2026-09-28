"use client";

import { useState } from "react";
import type { User } from "firebase/auth";

// Rond profielbolletje: Google-profielfoto als die er is, anders de eerste
// letter van de naam (of het e-mailadres).
export default function UserAvatar({ user, size = 32 }: { user: User; size?: number }) {
  const [imageFailed, setImageFailed] = useState(false);
  const initial = (user.displayName || user.email || "?").trim().charAt(0).toUpperCase();

  if (user.photoURL && !imageFailed) {
    return (
      // eslint-disable-next-line @next/next/no-img-element
      <img
        src={user.photoURL}
        alt=""
        width={size}
        height={size}
        // Google-profielfoto's weigeren soms verzoeken met een referrer.
        referrerPolicy="no-referrer"
        onError={() => setImageFailed(true)}
        className="rounded-full object-cover"
        style={{ width: size, height: size }}
      />
    );
  }

  return (
    <span
      className="rounded-full bg-primary text-on-primary flex items-center justify-center font-semibold select-none"
      style={{ width: size, height: size, fontSize: size * 0.45 }}
    >
      {initial}
    </span>
  );
}
