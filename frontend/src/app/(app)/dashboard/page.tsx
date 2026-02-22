"use client";

import React, { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { useAuthStore, useWorkspaceStore } from "@/lib/store";
import {
  Button,
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  EmptyState,
  Modal,
  Input,
  Badge,
  Skeleton,
  Avatar,
} from "@/components/ui";
import { formatRelative } from "@/lib/utils";
import type { Workspace } from "@/lib/types";
import {
  Plus,
  FolderKanban,
  ArrowRight,
  Users,
  Crown,
  Shield,
  UserCheck,
  Eye,
  Sparkles,
} from "lucide-react";

const roleIcons: Record<string, React.ReactNode> = {
  OWNER: <Crown className="h-3 w-3" />,
  ADMIN: <Shield className="h-3 w-3" />,
  MEMBER: <UserCheck className="h-3 w-3" />,
  VIEWER: <Eye className="h-3 w-3" />,
};

const roleColors: Record<string, string> = {
  OWNER: "bg-amber-50 text-amber-700",
  ADMIN: "bg-purple-50 text-purple-700",
  MEMBER: "bg-emerald-50 text-emerald-700",
  VIEWER: "bg-slate-100 text-slate-600",
};

export default function DashboardPage() {
  const { user } = useAuthStore();
  const { setCurrentWorkspace } = useWorkspaceStore();
  const router = useRouter();
  const queryClient = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [showJoin, setShowJoin] = useState(false);
  const [newName, setNewName] = useState("");
  const [joinToken, setJoinToken] = useState("");

  const {
    data: workspaces,
    isLoading,
  } = useQuery({
    queryKey: ["workspaces"],
    queryFn: () => api.workspaces.list(),
  });

  const createMutation = useMutation({
    mutationFn: (name: string) => api.workspaces.create({ name }),
    onSuccess: (ws) => {
      queryClient.invalidateQueries({ queryKey: ["workspaces"] });
      useWorkspaceStore.getState().fetchWorkspaces();
      setShowCreate(false);
      setNewName("");
      setCurrentWorkspace(ws);
      router.push(`/workspace/${ws.id}/boards`);
    },
  });

  const joinMutation = useMutation({
    mutationFn: (token: string) => api.workspaces.join(token),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workspaces"] });
      useWorkspaceStore.getState().fetchWorkspaces();
      setShowJoin(false);
      setJoinToken("");
    },
  });

  const greeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return "Good morning";
    if (hour < 17) return "Good afternoon";
    return "Good evening";
  };

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 mb-1">
              {greeting()}, {user?.name?.split(" ")[0]}
            </h1>
            <p className="text-slate-500">
              Here&apos;s what&apos;s happening across your workspaces.
            </p>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => setShowJoin(true)}>
              Join workspace
            </Button>
            <Button variant="primary" onClick={() => setShowCreate(true)}>
              <Plus className="h-4 w-4" />
              New workspace
            </Button>
          </div>
        </div>
      </div>

      {/* Quick stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <Card className="p-5">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center">
              <FolderKanban className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-bold text-slate-900">
                {workspaces?.length || 0}
              </p>
              <p className="text-xs text-slate-500">Workspaces</p>
            </div>
          </div>
        </Card>
        <Card className="p-5">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <Users className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-bold text-slate-900">
                {workspaces?.filter((w) => w.myRole === "OWNER").length || 0}
              </p>
              <p className="text-xs text-slate-500">Owned by you</p>
            </div>
          </div>
        </Card>
        <Card className="p-5">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-violet-50 text-violet-600 flex items-center justify-center">
              <Sparkles className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-bold text-slate-900">
                {workspaces?.filter((w) => w.myRole === "MEMBER" || w.myRole === "ADMIN").length || 0}
              </p>
              <p className="text-xs text-slate-500">Collaborating</p>
            </div>
          </div>
        </Card>
      </div>

      {/* Workspaces grid */}
      <div className="mb-4">
        <h2 className="text-lg font-semibold text-slate-900">Your workspaces</h2>
      </div>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[...Array(3)].map((_, i) => (
            <Card key={i} className="p-5">
              <Skeleton className="h-5 w-32 mb-3" />
              <Skeleton className="h-4 w-24 mb-4" />
              <Skeleton className="h-8 w-full" />
            </Card>
          ))}
        </div>
      ) : !workspaces || workspaces.length === 0 ? (
        <EmptyState
          icon={<FolderKanban className="h-8 w-8" />}
          title="No workspaces yet"
          description="Create your first workspace to get started, or join an existing one with an invite link."
          action={
            <Button variant="primary" onClick={() => setShowCreate(true)}>
              <Plus className="h-4 w-4" />
              Create workspace
            </Button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {workspaces.map((ws: Workspace) => (
            <Link
              key={ws.id}
              href={`/workspace/${ws.id}/boards`}
              onClick={() => setCurrentWorkspace(ws)}
            >
              <Card className="group hover:shadow-md hover:border-indigo-200 transition-all duration-300 cursor-pointer h-full">
                <CardHeader className="pb-3">
                  <div className="flex items-start justify-between">
                    <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500 flex items-center justify-center text-white font-bold text-lg">
                      {ws.name.charAt(0).toUpperCase()}
                    </div>
                    <span
                      className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${roleColors[ws.myRole]}`}
                    >
                      {roleIcons[ws.myRole]}
                      {ws.myRole}
                    </span>
                  </div>
                  <CardTitle className="mt-3 group-hover:text-indigo-700 transition-colors">
                    {ws.name}
                  </CardTitle>
                  <CardDescription>
                    Created {formatRelative(ws.createdAt)}
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between">
                    <span className="text-xs text-slate-400">
                      Updated {formatRelative(ws.updatedAt)}
                    </span>
                    <ArrowRight className="h-4 w-4 text-slate-300 group-hover:text-indigo-500 group-hover:translate-x-0.5 transition-all" />
                  </div>
                </CardContent>
              </Card>
            </Link>
          ))}

          {/* Create new card */}
          <button
            onClick={() => setShowCreate(true)}
            className="border-2 border-dashed border-slate-200 hover:border-indigo-300 rounded-xl p-6 flex flex-col items-center justify-center gap-3 text-slate-400 hover:text-indigo-600 transition-all duration-300 min-h-[180px] group"
          >
            <div className="h-12 w-12 rounded-xl bg-slate-50 group-hover:bg-indigo-50 flex items-center justify-center transition-colors">
              <Plus className="h-6 w-6" />
            </div>
            <span className="text-sm font-medium">New workspace</span>
          </button>
        </div>
      )}

      {/* Create workspace modal */}
      <Modal
        open={showCreate}
        onClose={() => setShowCreate(false)}
        title="Create workspace"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            createMutation.mutate(newName);
          }}
        >
          <Input
            label="Workspace name"
            placeholder="e.g. Product Team, Marketing"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            required
            autoFocus
          />
          <div className="flex justify-end gap-3 mt-6">
            <Button
              type="button"
              variant="ghost"
              onClick={() => setShowCreate(false)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              loading={createMutation.isPending}
            >
              Create
            </Button>
          </div>
        </form>
      </Modal>

      {/* Join workspace modal */}
      <Modal
        open={showJoin}
        onClose={() => setShowJoin(false)}
        title="Join workspace"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            joinMutation.mutate(joinToken);
          }}
        >
          <Input
            label="Invite token"
            placeholder="Paste the invite token here"
            value={joinToken}
            onChange={(e) => setJoinToken(e.target.value)}
            required
            autoFocus
          />
          <div className="flex justify-end gap-3 mt-6">
            <Button
              type="button"
              variant="ghost"
              onClick={() => setShowJoin(false)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              loading={joinMutation.isPending}
            >
              Join
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
