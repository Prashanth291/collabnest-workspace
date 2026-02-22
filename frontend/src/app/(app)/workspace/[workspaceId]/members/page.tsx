"use client";

import React, { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { useAuthStore } from "@/lib/store";
import { cn, formatRelative } from "@/lib/utils";
import {
  Button,
  Card,
  Avatar,
  Badge,
  Modal,
  Input,
  Select,
  EmptyState,
  Skeleton,
  Dropdown,
  DropdownItem,
} from "@/components/ui";
import type { WorkspaceMember, WorkspaceRole } from "@/lib/types";
import {
  Users,
  Plus,
  Crown,
  Shield,
  UserCheck,
  Eye,
  MoreHorizontal,
  UserMinus,
  ArrowUpDown,
  Mail,
  Copy,
  Check,
} from "lucide-react";

const roleConfig: Record<
  WorkspaceRole,
  { label: string; icon: React.ReactNode; color: string }
> = {
  OWNER: {
    label: "Owner",
    icon: <Crown className="h-3 w-3" />,
    color: "bg-amber-50 text-amber-700 border-amber-200",
  },
  ADMIN: {
    label: "Admin",
    icon: <Shield className="h-3 w-3" />,
    color: "bg-purple-50 text-purple-700 border-purple-200",
  },
  MEMBER: {
    label: "Member",
    icon: <UserCheck className="h-3 w-3" />,
    color: "bg-emerald-50 text-emerald-700 border-emerald-200",
  },
  VIEWER: {
    label: "Viewer",
    icon: <Eye className="h-3 w-3" />,
    color: "bg-slate-50 text-slate-600 border-slate-200",
  },
};

export default function MembersPage() {
  const params = useParams();
  const workspaceId = params.workspaceId as string;
  const { user } = useAuthStore();
  const queryClient = useQueryClient();
  const [showInvite, setShowInvite] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteRole, setInviteRole] = useState<WorkspaceRole>("MEMBER");
  const [inviteToken, setInviteToken] = useState("");
  const [copied, setCopied] = useState(false);

  const { data: members, isLoading } = useQuery({
    queryKey: ["members", workspaceId],
    queryFn: () => api.workspaces.getMembers(workspaceId),
    enabled: !!workspaceId,
  });

  const { data: workspace } = useQuery({
    queryKey: ["workspace", workspaceId],
    queryFn: () => api.workspaces.get(workspaceId),
    enabled: !!workspaceId,
  });

  const inviteMutation = useMutation({
    mutationFn: () =>
      api.workspaces.invite(workspaceId, {
        email: inviteEmail,
        role: inviteRole,
      }),
    onSuccess: (token) => {
      setInviteToken(typeof token === "string" ? token : "");
      setInviteEmail("");
    },
  });

  const changeRoleMutation = useMutation({
    mutationFn: ({
      userId,
      role,
    }: {
      userId: string;
      role: WorkspaceRole;
    }) => api.workspaces.changeMemberRole(workspaceId, userId, { role }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["members", workspaceId] });
    },
  });

  const removeMutation = useMutation({
    mutationFn: (userId: string) =>
      api.workspaces.removeMember(workspaceId, userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["members", workspaceId] });
    },
  });

  const copyToken = () => {
    navigator.clipboard.writeText(inviteToken);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const isAdmin =
    workspace?.myRole === "OWNER" || workspace?.myRole === "ADMIN";

  return (
    <div className="max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Members</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            {members?.length || 0} members in this workspace
          </p>
        </div>
        {isAdmin && (
          <Button variant="primary" onClick={() => setShowInvite(true)}>
            <Plus className="h-4 w-4" />
            Invite member
          </Button>
        )}
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[...Array(3)].map((_, i) => (
            <Card key={i} className="p-4 flex items-center gap-4">
              <Skeleton className="h-10 w-10 rounded-full" />
              <div className="flex-1">
                <Skeleton className="h-4 w-32 mb-2" />
                <Skeleton className="h-3 w-48" />
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <div className="space-y-2">
          {members?.map((member: WorkspaceMember) => (
            <Card
              key={member.id}
              className="p-4 flex items-center gap-4 hover:shadow-sm transition-shadow"
            >
              <Avatar name={member.name} size="md" />
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className="font-medium text-sm text-slate-900">
                    {member.name}
                  </span>
                  {member.userId === user?.id && (
                    <span className="text-xs text-slate-400">(you)</span>
                  )}
                </div>
                <div className="flex items-center gap-2 text-xs text-slate-500">
                  <span>@{member.username}</span>
                  <span className="text-slate-300">&middot;</span>
                  <span>{member.email}</span>
                </div>
              </div>

              <span
                className={cn(
                  "inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium border",
                  roleConfig[member.role].color
                )}
              >
                {roleConfig[member.role].icon}
                {roleConfig[member.role].label}
              </span>

              {isAdmin &&
                member.userId !== user?.id &&
                !member.isPrimaryOwner && (
                  <Dropdown
                    trigger={
                      <button className="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-50 transition-colors">
                        <MoreHorizontal className="h-4 w-4" />
                      </button>
                    }
                  >
                    {(["ADMIN", "MEMBER", "VIEWER"] as WorkspaceRole[])
                      .filter((r) => r !== member.role)
                      .map((r) => (
                        <DropdownItem
                          key={r}
                          onClick={() =>
                            changeRoleMutation.mutate({
                              userId: member.userId,
                              role: r,
                            })
                          }
                        >
                          <ArrowUpDown className="h-3.5 w-3.5 text-slate-400" />
                          Make {roleConfig[r].label}
                        </DropdownItem>
                      ))}
                    <DropdownItem
                      onClick={() => {
                        if (confirm(`Remove ${member.name}?`))
                          removeMutation.mutate(member.userId);
                      }}
                      className="text-red-600"
                    >
                      <UserMinus className="h-3.5 w-3.5" />
                      Remove
                    </DropdownItem>
                  </Dropdown>
                )}
            </Card>
          ))}
        </div>
      )}

      {/* Invite modal */}
      <Modal
        open={showInvite}
        onClose={() => {
          setShowInvite(false);
          setInviteToken("");
        }}
        title="Invite member"
      >
        {inviteToken ? (
          <div className="space-y-4">
            <p className="text-sm text-slate-500">
              Share this invite token with the person you want to invite:
            </p>
            <div className="flex items-center gap-2">
              <Input
                value={inviteToken}
                readOnly
                className="font-mono text-sm"
              />
              <Button variant="outline" onClick={copyToken}>
                {copied ? (
                  <Check className="h-4 w-4 text-emerald-500" />
                ) : (
                  <Copy className="h-4 w-4" />
                )}
              </Button>
            </div>
            <Button
              variant="ghost"
              className="w-full"
              onClick={() => {
                setInviteToken("");
                setShowInvite(false);
              }}
            >
              Done
            </Button>
          </div>
        ) : (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              inviteMutation.mutate();
            }}
            className="space-y-4"
          >
            <Input
              label="Email address"
              type="email"
              placeholder="colleague@example.com"
              value={inviteEmail}
              onChange={(e) => setInviteEmail(e.target.value)}
              required
              autoFocus
            />
            <Select
              label="Role"
              value={inviteRole}
              onChange={(e) => setInviteRole(e.target.value as WorkspaceRole)}
              options={[
                { value: "MEMBER", label: "Member" },
                { value: "VIEWER", label: "Viewer" },
                { value: "ADMIN", label: "Admin" },
              ]}
            />
            <div className="flex justify-end gap-3">
              <Button
                variant="ghost"
                onClick={() => setShowInvite(false)}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                type="submit"
                loading={inviteMutation.isPending}
              >
                <Mail className="h-4 w-4" />
                Send invite
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
