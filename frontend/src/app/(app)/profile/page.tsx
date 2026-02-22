"use client";

import React, { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { useAuthStore } from "@/lib/store";
import {
  Button,
  Card,
  CardContent,
  Input,
  Avatar,
  Badge,
} from "@/components/ui";
import type { User } from "@/lib/types";
import {
  User as UserIcon,
  Save,
  Mail,
  AtSign,
  Shield,
  Calendar,
  Check,
} from "lucide-react";

export default function ProfilePage() {
  const { user, setUser } = useAuthStore();
  const queryClient = useQueryClient();
  const [name, setName] = useState(user?.name || "");
  const [username, setUsername] = useState(user?.username || "");
  const [saved, setSaved] = useState(false);

  React.useEffect(() => {
    if (user) {
      setName(user.name || "");
      setUsername(user.username || "");
    }
  }, [user]);

  const updateMutation = useMutation({
    mutationFn: () => api.user.updateProfile({ name, username }),
    onSuccess: (updated: User) => {
      setUser(updated);
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    },
  });

  const hasChanges =
    user && (name !== (user.name || "") || username !== (user.username || ""));

  if (!user) return null;

  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-6">
        <h1 className="text-xl font-bold text-slate-900">Profile</h1>
        <p className="text-sm text-slate-500 mt-0.5">
          Manage your account settings
        </p>
      </div>

      <div className="space-y-6">
        {/* Avatar & identity */}
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center gap-5">
              <Avatar name={user.name || user.email} size="lg" />
              <div>
                <h2 className="text-lg font-semibold text-slate-900">
                  {user.name || "Unnamed"}
                </h2>
                <p className="text-sm text-slate-500">{user.email}</p>
                <div className="flex items-center gap-2 mt-2">
                  <Badge variant="info">{user.role}</Badge>
                  {user.authProvider && (
                    <Badge variant="default">
                      via {user.authProvider.toLowerCase()}
                    </Badge>
                  )}
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Edit profile */}
        <Card>
          <div className="p-5 border-b border-slate-100">
            <h2 className="text-sm font-semibold text-slate-900">
              Personal information
            </h2>
          </div>
          <CardContent className="p-5 space-y-4">
            <div>
              <label className="flex items-center gap-1.5 text-sm font-medium text-slate-700 mb-1.5">
                <UserIcon className="h-3.5 w-3.5" />
                Full name
              </label>
              <Input
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Your name"
              />
            </div>
            <div>
              <label className="flex items-center gap-1.5 text-sm font-medium text-slate-700 mb-1.5">
                <AtSign className="h-3.5 w-3.5" />
                Username
              </label>
              <Input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="username"
              />
            </div>
            <div>
              <label className="flex items-center gap-1.5 text-sm font-medium text-slate-700 mb-1.5">
                <Mail className="h-3.5 w-3.5" />
                Email
              </label>
              <Input value={user.email} disabled />
              <p className="text-xs text-slate-400 mt-1">
                Email cannot be changed.
              </p>
            </div>

            {(hasChanges || saved) && (
              <div className="flex justify-end">
                <Button
                  size="sm"
                  onClick={() => updateMutation.mutate()}
                  loading={updateMutation.isPending}
                  disabled={saved}
                >
                  {saved ? (
                    <>
                      <Check className="h-4 w-4 mr-1.5 text-emerald-500" />
                      Saved
                    </>
                  ) : (
                    <>
                      <Save className="h-4 w-4 mr-1.5" />
                      Save changes
                    </>
                  )}
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Account info */}
        <Card>
          <div className="p-5 border-b border-slate-100">
            <h2 className="text-sm font-semibold text-slate-900">
              Account details
            </h2>
          </div>
          <CardContent className="p-5">
            <dl className="space-y-3 text-sm">
              <div className="flex items-center justify-between">
                <dt className="text-slate-500 flex items-center gap-1.5">
                  <Shield className="h-3.5 w-3.5" />
                  Role
                </dt>
                <dd className="font-medium text-slate-900">{user.role}</dd>
              </div>
              <div className="flex items-center justify-between">
                <dt className="text-slate-500 flex items-center gap-1.5">
                  <Mail className="h-3.5 w-3.5" />
                  Auth provider
                </dt>
                <dd className="font-medium text-slate-900 capitalize">
                  {user.authProvider?.toLowerCase() || "local"}
                </dd>
              </div>
              {user.createdAt && (
                <div className="flex items-center justify-between">
                  <dt className="text-slate-500 flex items-center gap-1.5">
                    <Calendar className="h-3.5 w-3.5" />
                    Joined
                  </dt>
                  <dd className="font-medium text-slate-900">
                    {new Date(user.createdAt).toLocaleDateString("en-US", {
                      month: "long",
                      day: "numeric",
                      year: "numeric",
                    })}
                  </dd>
                </div>
              )}
            </dl>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
