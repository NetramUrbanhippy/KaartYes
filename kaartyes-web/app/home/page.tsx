"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";
import { subscribeToUserCards } from "@/lib/firestore";
import CardTile from "@/components/CardTile";
import Spinner from "@/components/Spinner";
import DropdownMenu, { DropdownMenuItem } from "@/components/DropdownMenu";
import { SearchIcon, CloseIcon, SortIcon } from "@/components/icons";
import UserAvatar from "@/components/UserAvatar";
import { cardDisplayName, type LoyaltyCard } from "@/lib/types";

type SortOrder = "az" | "za";

export default function HomePage() {
  const { user, loading, logout } = useAuth();
  const router = useRouter();
  const [cards, setCards] = useState<LoyaltyCard[]>([]);
  const [sortOrder, setSortOrder] = useState<SortOrder>("az");
  const [showSearch, setShowSearch] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [fetching, setFetching] = useState(true);

  useEffect(() => {
    if (!loading && !user) {
      router.replace("/login");
    }
  }, [user, loading, router]);

  useEffect(() => {
    if (!user) return;
    const unsubscribe = subscribeToUserCards(user.uid, (loaded) => {
      setCards(loaded);
      setFetching(false);
    });
    return unsubscribe;
  }, [user]);

  const query = searchQuery.trim().toLowerCase();
  const filtered = query
    ? cards.filter((card) => {
        const haystack = `${card.name} ${card.nickname ?? ""} ${card.notes ?? ""}`.toLowerCase();
        return haystack.includes(query);
      })
    : cards;

  const sorted = [...filtered].sort((a, b) => {
    if (a.isFavorite !== b.isFavorite) return a.isFavorite ? -1 : 1;
    const nameA = cardDisplayName(a).toLowerCase();
    const nameB = cardDisplayName(b).toLowerCase();
    return sortOrder === "az" ? nameA.localeCompare(nameB) : nameB.localeCompare(nameA);
  });

  if (loading || fetching) {
    return <Spinner />;
  }

  return (
    <div className="min-h-screen bg-background">
      <header className="px-4 pt-safe-top pb-2 sticky top-0 z-10 bg-background">
        <div className="flex items-center gap-2 min-h-11">
          {showSearch ? (
            <input
              autoFocus
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Zoeken.."
              className="flex-1 rounded-full border border-outline px-4 py-2 text-sm outline-none focus:border-primary bg-surface"
            />
          ) : (
            <h1 className="flex-1 text-2xl font-bold text-foreground">Loyaliteitskaarten</h1>
          )}

          <button
            onClick={() => {
              setShowSearch((v) => !v);
              if (showSearch) setSearchQuery("");
            }}
            aria-label="Zoeken"
            className="w-11 h-11 flex items-center justify-center rounded-full hover:bg-surface-variant transition-colors shrink-0 text-foreground"
          >
            {showSearch ? <CloseIcon /> : <SearchIcon />}
          </button>

          <DropdownMenu
            trigger={(toggle) => (
              <button
                onClick={toggle}
                aria-label="Sorteren"
                className="w-11 h-11 flex items-center justify-center rounded-full hover:bg-surface-variant transition-colors shrink-0 text-foreground"
              >
                <SortIcon />
              </button>
            )}
          >
            {(close) => (
              <>
                <DropdownMenuItem
                  label="A → Z"
                  checked={sortOrder === "az"}
                  onClick={() => {
                    setSortOrder("az");
                    close();
                  }}
                />
                <DropdownMenuItem
                  label="Z → A"
                  checked={sortOrder === "za"}
                  onClick={() => {
                    setSortOrder("za");
                    close();
                  }}
                />
              </>
            )}
          </DropdownMenu>

          <DropdownMenu
            trigger={(toggle) => (
              <button
                onClick={toggle}
                aria-label="Account"
                className="w-11 h-11 flex items-center justify-center rounded-full hover:bg-surface-variant transition-colors shrink-0"
              >
                {user && <UserAvatar user={user} />}
              </button>
            )}
          >
            {(close) => (
              <>
                {user && (user.displayName || user.email) && (
                  <div className="px-4 pt-2 pb-2.5 mb-1 border-b border-outline">
                    {user.displayName && (
                      <p className="text-sm font-medium text-foreground truncate">{user.displayName}</p>
                    )}
                    {user.email && <p className="text-xs text-secondary truncate">{user.email}</p>}
                  </div>
                )}
                <DropdownMenuItem
                  label="Uitloggen"
                  onClick={() => {
                    close();
                    logout();
                  }}
                />
              </>
            )}
          </DropdownMenu>
        </div>
      </header>

      <main className="px-4 pb-24">
        {sorted.length === 0 ? (
          <div className="flex flex-col items-center justify-center mt-24 gap-2 text-center">
            <p className="font-semibold text-foreground">Nog geen kaarten</p>
            {!query && (
              <p className="text-sm text-secondary">
                Tik op + om je eerste loyaliteitskaart toe te voegen
              </p>
            )}
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3 pt-2">
            {sorted.map((card) => (
              <CardTile
                key={card.id}
                card={card}
                onClick={() => router.push(`/card?id=${card.id}`)}
              />
            ))}
          </div>
        )}
      </main>

      <div className="fixed bottom-6 right-6">
        <button
          onClick={() => router.push("/card/new")}
          className="w-14 h-14 bg-primary text-on-primary rounded-full shadow-lg flex items-center justify-center text-3xl active:scale-95 transition-transform"
          aria-label="Kaart toevoegen"
        >
          +
        </button>
      </div>
    </div>
  );
}
