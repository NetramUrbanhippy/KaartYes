"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import Spinner from "@/components/Spinner";

export default function LoginPage() {
  const { user, loading, authError, signInWithGoogle } = useAuth();
  const router = useRouter();
  const [signingIn, setSigningIn] = useState(false);

  useEffect(() => {
    if (!loading && user) {
      router.replace("/home");
    }
  }, [user, loading, router]);

  if (loading) {
    return <Spinner />;
  }

  async function handleSignIn() {
    setSigningIn(true);
    try {
      await signInWithGoogle();
    } finally {
      setSigningIn(false);
    }
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-background px-10">
      <div className="flex flex-col items-center gap-4 max-w-sm w-full">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src="/icons/icon-512.png" alt="KaartYes" className="w-28 h-28 rounded-2xl" />

        <h1 className="text-3xl font-bold text-foreground text-center">KaartYes</h1>
        <p className="text-base text-secondary text-center">
          Jouw kaarten, gratis opgeslagen. Snel en zonder reclame.
        </p>

        {authError && (
          <p className="text-sm text-error text-center">{authError}</p>
        )}

        <button
          onClick={handleSignIn}
          disabled={signingIn}
          className="w-full h-[52px] mt-4 rounded-xl bg-primary text-on-primary font-medium flex items-center justify-center gap-3 active:opacity-90 transition-opacity disabled:opacity-70"
        >
          {signingIn ? (
            <>
              <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
              Bezig met inloggen…
            </>
          ) : (
            "Inloggen met Google"
          )}
        </button>
      </div>
    </div>
  );
}
