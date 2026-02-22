"use client";

import React, { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { formatRelative, cn } from "@/lib/utils";
import {
  Button,
  Card,
  EmptyState,
  Skeleton,
  Badge,
} from "@/components/ui";
import type {
  NotificationDto,
  NotificationStats,
  PageResponse,
} from "@/lib/types";
import {
  Bell,
  BellOff,
  CheckCheck,
  ChevronLeft,
  ChevronRight,
  MessageSquare,
  UserPlus,
  CheckCircle,
  AlertCircle,
  Info,
  Star,
  MailOpen,
} from "lucide-react";

const notifIcons: Record<string, React.ReactNode> = {
  TASK_ASSIGNED: <Star className="h-4 w-4" />,
  TASK_UPDATED: <Info className="h-4 w-4" />,
  TASK_COMPLETED: <CheckCircle className="h-4 w-4" />,
  COMMENT_ADDED: <MessageSquare className="h-4 w-4" />,
  MEMBER_ADDED: <UserPlus className="h-4 w-4" />,
  WORKSPACE_INVITE: <UserPlus className="h-4 w-4" />,
  MENTION: <AlertCircle className="h-4 w-4" />,
};

const notifColors: Record<string, string> = {
  TASK_ASSIGNED: "bg-indigo-50 text-indigo-600",
  TASK_UPDATED: "bg-blue-50 text-blue-600",
  TASK_COMPLETED: "bg-emerald-50 text-emerald-600",
  COMMENT_ADDED: "bg-sky-50 text-sky-600",
  MEMBER_ADDED: "bg-teal-50 text-teal-600",
  WORKSPACE_INVITE: "bg-violet-50 text-violet-600",
  MENTION: "bg-amber-50 text-amber-600",
};

export default function NotificationsPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [filter, setFilter] = useState<"all" | "unread">("all");

  const { data: stats } = useQuery<NotificationStats>({
    queryKey: ["notificationStats"],
    queryFn: () => api.notifications.getStats(),
  });

  const { data, isLoading } = useQuery<PageResponse<NotificationDto>>({
    queryKey: ["notifications", page],
    queryFn: () => api.notifications.list(page),
  });

  const markReadMutation = useMutation({
    mutationFn: (id: string) => api.notifications.markRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      queryClient.invalidateQueries({ queryKey: ["notificationStats"] });
    },
  });

  const markAllReadMutation = useMutation({
    mutationFn: () => api.notifications.markAllRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      queryClient.invalidateQueries({ queryKey: ["notificationStats"] });
    },
  });

  const notifications = data?.content || [];
  const totalPages = data?.totalPages || 0;
  const filtered =
    filter === "unread"
      ? notifications.filter((n) => !n.isRead)
      : notifications;

  return (
    <div className="max-w-3xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Notifications</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            {stats?.unreadCount || 0} unread
          </p>
        </div>
        {(stats?.unreadCount || 0) > 0 && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => markAllReadMutation.mutate()}
            loading={markAllReadMutation.isPending}
          >
            <CheckCheck className="h-4 w-4 mr-1.5" />
            Mark all read
          </Button>
        )}
      </div>

      {/* Filter tabs */}
      <div className="flex items-center gap-1 mb-4 p-1 bg-slate-100 rounded-lg w-fit">
        <button
          onClick={() => setFilter("all")}
          className={cn(
            "px-3 py-1.5 text-sm font-medium rounded-md transition-colors",
            filter === "all"
              ? "bg-white text-slate-900 shadow-sm"
              : "text-slate-500 hover:text-slate-700"
          )}
        >
          All
        </button>
        <button
          onClick={() => setFilter("unread")}
          className={cn(
            "px-3 py-1.5 text-sm font-medium rounded-md transition-colors",
            filter === "unread"
              ? "bg-white text-slate-900 shadow-sm"
              : "text-slate-500 hover:text-slate-700"
          )}
        >
          Unread
          {(stats?.unreadCount || 0) > 0 && (
            <span className="ml-1.5 px-1.5 py-0.5 text-xs bg-indigo-100 text-indigo-700 rounded-full">
              {stats?.unreadCount}
            </span>
          )}
        </button>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => (
            <Card key={i} className="p-4 flex items-start gap-3">
              <Skeleton className="h-8 w-8 rounded-full" />
              <div className="flex-1">
                <Skeleton className="h-4 w-3/4 mb-2" />
                <Skeleton className="h-3 w-32" />
              </div>
            </Card>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={
            filter === "unread" ? (
              <BellOff className="h-8 w-8" />
            ) : (
              <Bell className="h-8 w-8" />
            )
          }
          title={
            filter === "unread"
              ? "All caught up!"
              : "No notifications"
          }
          description={
            filter === "unread"
              ? "You have no unread notifications."
              : "Notifications will appear here when you get them."
          }
        />
      ) : (
        <>
          <div className="space-y-2">
            {filtered.map((n) => {
              const iconColor =
                notifColors[n.notificationType] || "bg-slate-100 text-slate-500";
              const icon =
                notifIcons[n.notificationType] || <Bell className="h-4 w-4" />;

              return (
                <Card
                  key={n.id}
                  className={cn(
                    "p-4 flex items-start gap-3 transition-all cursor-pointer group",
                    !n.isRead
                      ? "bg-indigo-50/30 border-indigo-100 hover:bg-indigo-50/50"
                      : "hover:bg-slate-50"
                  )}
                  onClick={() => {
                    if (!n.isRead) markReadMutation.mutate(n.id);
                  }}
                >
                  <div
                    className={cn(
                      "h-8 w-8 rounded-full flex items-center justify-center flex-shrink-0",
                      iconColor
                    )}
                  >
                    {icon}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-slate-700">{n.message}</p>
                    <p className="text-xs text-slate-400 mt-0.5">
                      {formatRelative(n.createdAt)}
                    </p>
                  </div>
                  {!n.isRead && (
                    <div className="h-2 w-2 rounded-full bg-indigo-500 mt-2 flex-shrink-0" />
                  )}
                  {n.isRead && (
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity">
                      <MailOpen className="h-4 w-4 text-slate-300" />
                    </div>
                  )}
                </Card>
              );
            })}
          </div>

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-6">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="p-2 rounded-lg hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              <span className="text-sm text-slate-500">
                {page + 1} / {totalPages}
              </span>
              <button
                onClick={() =>
                  setPage((p) => Math.min(totalPages - 1, p + 1))
                }
                disabled={page >= totalPages - 1}
                className="p-2 rounded-lg hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
