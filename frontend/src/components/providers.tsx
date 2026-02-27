"use client";

import React, { useEffect } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useAuthStore } from "@/lib/store";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function AuthInitializer({ children }: { children: React.ReactNode }) {
  const { fetchUser, isLoading, token, isHydrated, setHydrated } = useAuthStore();

  useEffect(() => {
    // Hydrate token from localStorage on mount (client-only)
    const stored = localStorage.getItem("token");
    if (stored) {
      useAuthStore.setState({ token: stored, isLoading: true });
    }

    // Check for OAuth2 token in URL
    const params = new URLSearchParams(window.location.search);
    const urlToken = params.get("token");
    if (urlToken) {
      useAuthStore.getState().login(urlToken);
      window.history.replaceState({}, "", window.location.pathname);
      setHydrated(true);
      return;
    }

    fetchUser().finally(() => setHydrated(true));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  if (!isHydrated && token === null) {
    // First render — matches SSR output (no token on server)
    return <>{children}</>;
  }

  if (isLoading && token) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="flex flex-col items-center gap-3">
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-slate-200 border-t-indigo-600" />
          <p className="text-sm text-slate-500">Loading...</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthInitializer>{children}</AuthInitializer>
    </QueryClientProvider>
  );
}
