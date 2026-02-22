"use client";

import React from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { formatRelative } from "@/lib/utils";
import {
  Button,
  Card,
  EmptyState,
  Skeleton,
  Badge,
} from "@/components/ui";
import type { FileResponse } from "@/lib/types";
import {
  HardDrive,
  FileText,
  Image as ImageIcon,
  File,
  Download,
  Trash2,
  ExternalLink,
} from "lucide-react";

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1048576) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1048576).toFixed(1)} MB`;
}

function getFileIcon(fileName: string) {
  const ext = fileName.split(".").pop()?.toLowerCase();
  if (["jpg", "jpeg", "png", "gif", "svg", "webp"].includes(ext || ""))
    return <ImageIcon className="h-5 w-5" />;
  if (["pdf", "doc", "docx", "txt", "md"].includes(ext || ""))
    return <FileText className="h-5 w-5" />;
  return <File className="h-5 w-5" />;
}

export default function FilesPage() {
  const params = useParams();
  const workspaceId = params.workspaceId as string;
  const queryClient = useQueryClient();

  const { data: files, isLoading } = useQuery({
    queryKey: ["files", workspaceId],
    queryFn: () => api.files.list(workspaceId),
    enabled: !!workspaceId,
  });

  const deleteMutation = useMutation({
    mutationFn: (fileId: string) => api.files.delete(workspaceId, fileId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["files", workspaceId] });
    },
  });

  return (
    <div className="max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Files</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            {files?.length || 0} files in this workspace
          </p>
        </div>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(3)].map((_, i) => (
            <Card key={i} className="p-4 flex items-center gap-4">
              <Skeleton className="h-10 w-10 rounded-xl" />
              <div className="flex-1">
                <Skeleton className="h-4 w-48 mb-2" />
                <Skeleton className="h-3 w-24" />
              </div>
            </Card>
          ))}
        </div>
      ) : !files || files.length === 0 ? (
        <EmptyState
          icon={<HardDrive className="h-8 w-8" />}
          title="No files yet"
          description="Files uploaded to tasks and documents will appear here."
        />
      ) : (
        <div className="space-y-2">
          {files.map((file: FileResponse) => (
            <Card
              key={file.id}
              className="p-4 flex items-center gap-4 hover:shadow-sm transition-shadow group"
            >
              <div className="h-10 w-10 rounded-xl bg-sky-50 text-sky-600 flex items-center justify-center flex-shrink-0">
                {getFileIcon(file.fileName)}
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="text-sm font-medium text-slate-900 truncate">
                  {file.fileName}
                </h3>
                <div className="flex items-center gap-3 text-xs text-slate-400 mt-0.5">
                  <span>{formatFileSize(file.size)}</span>
                  <span className="text-slate-300">&middot;</span>
                  <span>by {file.uploadedByUsername}</span>
                  <span className="text-slate-300">&middot;</span>
                  <span>{formatRelative(file.createdAt)}</span>
                </div>
              </div>
              <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                <a
                  href={file.fileUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="p-2 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors"
                >
                  <ExternalLink className="h-4 w-4" />
                </a>
                <button
                  onClick={() => {
                    if (confirm("Delete this file?"))
                      deleteMutation.mutate(file.id);
                  }}
                  className="p-2 rounded-lg hover:bg-red-50 text-slate-400 hover:text-red-600 transition-colors"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
