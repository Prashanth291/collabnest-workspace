import { create } from "zustand";
import type { User, Workspace } from "./types";
import api from "./api";

interface AuthState {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  setToken: (token: string) => void;
  setUser: (user: User) => void;
  login: (token: string) => Promise<void>;
  logout: () => void;
  fetchUser: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  token: null,
  isLoading: false,
  isAuthenticated: false,

  setToken: (token: string) => {
    localStorage.setItem("token", token);
    set({ token, isAuthenticated: true });
  },

  setUser: (user: User) => {
    set({ user });
  },

  login: async (token: string) => {
    localStorage.setItem("token", token);
    set({ token, isAuthenticated: true });
    try {
      const user = await api.user.getProfile();
      set({ user, isLoading: false });
    } catch {
      get().logout();
    }
  },

  logout: () => {
    localStorage.removeItem("token");
    set({ user: null, token: null, isAuthenticated: false, isLoading: false });
  },

  fetchUser: async () => {
    const token = get().token;
    if (!token) {
      set({ isLoading: false });
      return;
    }
    try {
      const user = await api.user.getProfile();
      set({ user, isAuthenticated: true, isLoading: false });
    } catch {
      get().logout();
    }
  },
}));

// ─── Workspace Store ─────────────────────────────────────────
interface WorkspaceState {
  workspaces: Workspace[];
  currentWorkspace: Workspace | null;
  isLoading: boolean;
  setCurrentWorkspace: (workspace: Workspace | null) => void;
  fetchWorkspaces: () => Promise<void>;
}

export const useWorkspaceStore = create<WorkspaceState>((set) => ({
  workspaces: [],
  currentWorkspace: null,
  isLoading: false,

  setCurrentWorkspace: (workspace) => set({ currentWorkspace: workspace }),

  fetchWorkspaces: async () => {
    set({ isLoading: true });
    try {
      const workspaces = await api.workspaces.list();
      set({ workspaces, isLoading: false });
    } catch {
      set({ isLoading: false });
    }
  },
}));
