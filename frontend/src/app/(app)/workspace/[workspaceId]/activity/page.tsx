"use client";

import React, { useState } from "react";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { formatRelative, getInitials, cn } from "@/lib/utils";
import {
  Card,
  EmptyState,
  Skeleton,
  Avatar,
  Badge,
} from "@/components/ui";
import type { ActivityLogDto, PageResponse } from "@/lib/types";
import {
  Activity,
  Plus,
  Edit3,
  Trash2,
  UserPlus,
  UserMinus,
  MessageSquare,
  CheckCircle,
  ArrowRight,
  FileText,
  Columns,
  LayoutGrid,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";

const activityIcons: Record<string, React.ReactNode> = {
  TASK_CREATED: <Plus className="h-4 w-4" />, 
  TASK_UPDATED: <Edit3 className="h-4 w-4" />,
  TASK_DELETED: <Trash2 className="h-4 w-4" />,
  TASK_MOVED: <ArrowRight className="h-4 w-4" />,
  TASK_ASSIGNED: <UserPlus className="h-4 w-4" />,
  TASK_COMPLETED: <CheckCircle className="h-4 w-4" />,
  COMMENT_ADDED: <MessageSquare className="h-4 w-4" />,
  DOCUMENT_CREATED: <FileText className="h-4 w-4" />,
  DOCUMENT_UPDATED: <Edit3 className="h-4 w-4" />,
  COLUMN_CREATED: <Columns className="h-4 w-4" />,
  BOARD_CREATED: <LayoutGrid className="h-4 w-4" />,
  MEMBER_ADDED: <UserPlus className="h-4 w-4" />,
  MEMBER_REMOVED: <UserMinus className="h-4 w-4" />,
};

const activityColors: Record<string, string> = {
  TASK_CREATED: "bg-emerald-50 text-emerald-600",
  TASK_UPDATED: "bg-blue-50 text-blue-600",
  TASK_DELETED: "bg-red-50 text-red-600",
  TASK_MOVED: "bg-amber-50 text-amber-600",
  TASK_ASSIGNED: "bg-indigo-50 text-indigo-600",
  TASK_COMPLETED: "bg-emerald-50 text-emerald-600",
  COMMENT_ADDED: "bg-sky-50 text-sky-600",
  DOCUMENT_CREATED: "bg-violet-50 text-violet-600",
  DOCUMENT_UPDATED: "bg-violet-50 text-violet-600",
  COLUMN_CREATED: "bg-slate-100 text-slate-600",
  BOARD_CREATED: "bg-indigo-50 text-indigo-600",
  MEMBER_ADDED: "bg-teal-50 text-teal-600",
  MEMBER_REMOVED: "bg-orange-50 text-orange-600",
};

export default function ActivityPage() {
  const params = useParams();
  const workspaceId = params.workspaceId as string;
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery<PageResponse<ActivityLogDto>>({
    queryKey: ["activity", workspaceId, page],
    queryFn: () => api.activity.workspace(workspaceId, page),
    enabled: !!workspaceId,
  });

  const activities = data?.content || [];
  const totalPages = data?.totalPages || 0;

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-xl font-bold text-slate-900">Activity</h1>
        <p className="text-sm text-slate-500 mt-0.5">Team activity log</p>
      </div>

      {isLoading ? (
        <div className="space-y-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="flex items-start gap-3">
              <Skeleton className="h-8 w-8 rounded-full" />
              <div>
                <Skeleton className="h-4 w-64 mb-1" />
                <Skeleton className="h-3 w-32" />
              </div>
            </div>
          ))}
        </div>
      ) : activities.length === 0 ? (
        <EmptyState
          icon={<Activity className="h-8 w-8" />}
          title="No activity yet"
          description="Actions performed in this workspace will appear here."
        />
      ) : (
        <>
          <div className="relative pl-6">
            {/* timeline line */}
            <div className="absolute left-[15px] top-0 bottom-0 w-px bg-slate-200" />

            <div className="space-y-6">
              {activities.map((a) => {
                const iconColor =
                  activityColors[a.activityType] ||
                  "bg-slate-100 text-slate-500";
                const icon =
                  activityIcons[a.activityType] ||
                  <Activity className="h-4 w-4" />;

                return (
                  <div
                    key={a.id}
                    className="relative flex items-start gap-3"
                  >
                    {/* timeline dot */}
                    <div
                      className={cn(
                        "absolute -left-6 top-0 z-10 h-8 w-8 rounded-full flex items-center justify-center ring-4 ring-white",
                        iconColor
                      )}
                    >
                      {icon}
                    </div>

                    <div className="ml-6">
                      <p className="text-sm text-slate-700">
                        <span className="font-medium text-slate-900">
                          {a.userName}
                        </span>{" "}
                        {a.description}
                      </p>
                      <p className="text-xs text-slate-400 mt-0.5">
                        {formatRelative(a.createdAt)}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {totalPages > 1 && (
            <div className="flex items-center justify-center gap-2 mt-8">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="p-2 rounded-lg hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              <span className="text-sm text-slate-500">
                Page {page + 1} of {totalPages}
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
