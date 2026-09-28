import type { Metadata, Viewport } from "next";
import { Geist } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/contexts/AuthContext";
import { ThemeProvider } from "@/contexts/ThemeContext";
import ServiceWorkerRegister from "@/components/ServiceWorkerRegister";

// Zet de .dark class vóór de eerste render, zodat er geen flits van het
// verkeerde thema is terwijl React nog aan het opstarten is.
const themeInitScript = `
(function () {
  try {
    // Oude handmatige keuze opruimen: het thema volgt nu altijd het systeem.
    localStorage.removeItem("theme");
  } catch (e) {}
  try {
    var dark = window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches;
    if (dark) document.documentElement.classList.add("dark");
  } catch (e) {}
})();
`;

const geist = Geist({
  variable: "--font-geist",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "KaartYes",
  description: "Jouw loyaliteitskaarten altijd bij de hand",
  manifest: "/manifest.json",
  appleWebApp: {
    capable: true,
    statusBarStyle: "default",
    title: "KaartYes",
  },
};

export const viewport: Viewport = {
  themeColor: "#f2f2f2",
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
  userScalable: false,
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="nl" className={geist.variable}>
      <head>
        <script dangerouslySetInnerHTML={{ __html: themeInitScript }} />
      </head>
      <body className="min-h-screen bg-background text-foreground antialiased">
        <ServiceWorkerRegister />
        <ThemeProvider>
          <AuthProvider>{children}</AuthProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
