"use client";

import React, { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { useWorkspaceStore } from "@/lib/store";
import { Button, Card, CardContent, Input, Modal, Badge } from "@/components/ui";
import type { Workspace } from "@/lib/types";
import {
  Settings as SettingsIcon,
  Save,
  Trash2,
  AlertTriangle,
  Copy,
  Check,
  Link as LinkIcon,
  Shield,
} from "lucide-react";

export default function SettingsPage() {
  const params = useParams();
  const router = useRouter();
  const workspaceId = params.workspaceId as string;
  const queryClient = useQueryClient();
  const { currentWorkspace, setCurrentWorkspace } = useWorkspaceStore();

  const { data: workspace } = useQuery<Workspace>({
    queryKey: ["workspace", workspaceId],
    queryFn: () => api.workspaces.get(workspaceId),
    enabled: !!workspaceId,
  });

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [showDelete, setShowDelete] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState("");
  const [copied, setCopied] = useState(false);

  React.useEffect(() => {
    if (workspace) {
      setName(workspace.name);
      setDescription(workspace.description || "");
    }
  }, [workspace]);

  const updateMutation = useMutation({
    mutationFn: () =>
      api.workspaces.update(workspaceId, { name, description }),
    onSuccess: (updated: Workspace) => {
      queryClient.invalidateQueries({ queryKey: ["workspace", workspaceId] });
      queryClient.invalidateQueries({ queryKey: ["workspaces"] });
      if (currentWorkspace?.id === workspaceId) setCurrentWorkspace(updated);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => api.workspaces.delete(workspaceId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["workspaces"] });
      router.push("/dashboard");
    },
  });

  const inviteToken = workspace?.inviteToken;

  const copyInviteLink = () => {
    if (!inviteToken) return;
    navigator.clipboard.writeText(inviteToken);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const hasChanges =
    workspace && (name !== workspace.name || description !== (workspace.description || ""));

  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-6">
        <h1 className="text-xl font-bold text-slate-900">Settings</h1>
        <p className="text-sm text-slate-500 mt-0.5">
          Manage workspace settings
        </p>
      </div>

      <div className="space-y-6">
        {/* General */}
        <Card>
          <div className="p-5 border-b border-slate-100">
            <h2 className="text-sm font-semibold text-slate-900">General</h2>
          </div>
          <CardContent className="p-5 space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">
                Workspace name
              </label>
              <Input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="My Workspace"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">
                Description
              </label>
              <Input
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="A brief description..."
              />
            </div>
            {hasChanges && (
              <div className="flex justify-end">
                <Button
                  size="sm"
                  onClick={() => updateMutation.mutate()}
                  loading={updateMutation.isPending}
                >
                  <Save className="h-4 w-4 mr-1.5" />
                  Save changes
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Invite Token */}
        <Card>
          <div className="p-5 border-b border-slate-100">
            <h2 className="text-sm font-semibold text-slate-900">
              Invite token
            </h2>
          </div>
          <CardContent className="p-5">
            <p className="text-sm text-slate-500 mb-3">
              Share this token so others can join your workspace.
            </p>
            {inviteToken ? (
              <div className="flex items-center gap-2">
                <div className="flex-1 flex items-center gap-2 bg-slate-50 rounded-lg border border-slate-200 px-3 py-2">
                  <LinkIcon className="h-4 w-4 text-slate-400 flex-shrink-0" />
                  <code className="text-sm text-slate-700 font-mono truncate">
                    {inviteToken}
                  </code>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={copyInviteLink}
                >
                  {copied ? (
                    <Check className="h-4 w-4 text-emerald-600" />
                  ) : (
                    <Copy className="h-4 w-4" />
                  )}
                </Button>
              </div>
            ) : (
              <p className="text-sm text-slate-400 italic">
                No invite token available.
              </p>
            )}
          </CardContent>
        </Card>

        {/* Danger zone */}
        <Card className="border-red-200">
          <div className="p-5 border-b border-red-100 bg-red-50/50 rounded-t-xl">
            <div className="flex items-center gap-2">
              <AlertTriangle className="h-4 w-4 text-red-500" />
              <h2 className="text-sm font-semibold text-red-700">
                Danger zone
              </h2>
            </div>
          </div>
          <CardContent className="p-5">
            <p className="text-sm text-slate-500 mb-3">
              Permanently delete this workspace and all its data. This action
              cannot be undone.
            </p>
            <Button
              variant="destructive"
              size="sm"
              onClick={() => setShowDelete(true)}
            >
              <Trash2 className="h-4 w-4 mr-1.5" />
              Delete workspace
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* Delete confirmation modal */}
      <Modal
        open={showDelete}
        onClose={() => {
          setShowDelete(false);
          setDeleteConfirm("");
        }}
        title="Delete workspace"
      >
        <div className="space-y-4">
          <div className="p-3 bg-red-50 rounded-lg border border-red-100">
            <div className="flex items-start gap-2">
              <AlertTriangle className="h-5 w-5 text-red-500 mt-0.5 flex-shrink-0" />
              <p className="text-sm text-red-700">
                This will permanently delete the workspace, all boards, tasks,
                documents, and messages. This cannot be undone.
              </p>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1.5">
              Type <span className="font-mono text-red-600">delete</span> to
              confirm
            </label>
            <Input
              value={deleteConfirm}
              onChange={(e) => setDeleteConfirm(e.target.value)}
              placeholder="delete"
            />
          </div>
          <div className="flex justify-end gap-2">
            <Button
              variant="ghost"
              onClick={() => {
                setShowDelete(false);
                setDeleteConfirm("");
              }}
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={() => deleteMutation.mutate()}
              disabled={deleteConfirm !== "delete"}
              loading={deleteMutation.isPending}
            >
              Delete workspace
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
