"use client";

import React, { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { formatRelative } from "@/lib/utils";
import {
  Button,
  Card,
  EmptyState,
  Modal,
  Input,
  Textarea,
  Skeleton,
  Avatar,
  Badge,
} from "@/components/ui";
import type { Document as DocType } from "@/lib/types";
import {
  FileText,
  Plus,
  Clock,
  ArrowRight,
  Edit2,
  Trash2,
  Search,
} from "lucide-react";

export default function DocumentsPage() {
  const params = useParams();
  const workspaceId = params.workspaceId as string;
  const router = useRouter();
  const queryClient = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [docForm, setDocForm] = useState({ title: "", content: "" });
  const [search, setSearch] = useState("");

  const { data: documents, isLoading } = useQuery({
    queryKey: ["documents", workspaceId],
    queryFn: () => api.documents.list(workspaceId),
    enabled: !!workspaceId,
  });

  const createMutation = useMutation({
    mutationFn: () =>
      api.documents.create(workspaceId, {
        title: docForm.title,
        content: docForm.content,
      }),
    onSuccess: (doc) => {
      queryClient.invalidateQueries({ queryKey: ["documents", workspaceId] });
      setShowCreate(false);
      setDocForm({ title: "", content: "" });
      router.push(`/workspace/${workspaceId}/documents/${doc.id}`);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (docId: string) => api.documents.delete(workspaceId, docId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["documents", workspaceId] });
    },
  });

  const filtered = documents?.filter((d: DocType) =>
    d.title.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="max-w-5xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Documents</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            {documents?.length || 0} documents in this workspace
          </p>
        </div>
        <Button variant="primary" onClick={() => setShowCreate(true)}>
          <Plus className="h-4 w-4" />
          New document
        </Button>
      </div>

      {/* Search */}
      {documents && documents.length > 0 && (
        <div className="relative mb-4">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search documents..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 bg-white text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
          />
        </div>
      )}

      {isLoading ? (
        <div className="space-y-3">
          {[...Array(3)].map((_, i) => (
            <Card key={i} className="p-5">
              <Skeleton className="h-5 w-48 mb-3" />
              <Skeleton className="h-4 w-full mb-2" />
              <Skeleton className="h-3 w-32" />
            </Card>
          ))}
        </div>
      ) : !filtered || filtered.length === 0 ? (
        <EmptyState
          icon={<FileText className="h-8 w-8" />}
          title={search ? "No matching documents" : "No documents yet"}
          description={
            search
              ? "Try a different search term."
              : "Create your first document to start writing and collaborating."
          }
          action={
            !search ? (
              <Button variant="primary" onClick={() => setShowCreate(true)}>
                <Plus className="h-4 w-4" />
                Create document
              </Button>
            ) : undefined
          }
        />
      ) : (
        <div className="space-y-2">
          {filtered.map((doc: DocType) => (
            <Card
              key={doc.id}
              className="group p-5 hover:shadow-md hover:border-indigo-200 transition-all duration-300 cursor-pointer"
              onClick={() =>
                router.push(
                  `/workspace/${workspaceId}/documents/${doc.id}`
                )
              }
            >
              <div className="flex items-start justify-between">
                <div className="flex items-start gap-3 flex-1 min-w-0">
                  <div className="h-10 w-10 rounded-xl bg-sky-50 text-sky-600 flex items-center justify-center flex-shrink-0">
                    <FileText className="h-5 w-5" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="font-semibold text-slate-900 group-hover:text-indigo-700 transition-colors">
                      {doc.title}
                    </h3>
                    {doc.content && (
                      <p className="text-sm text-slate-500 mt-1 line-clamp-2">
                        {doc.content.slice(0, 200)}
                      </p>
                    )}
                    <div className="flex items-center gap-3 mt-2 text-xs text-slate-400">
                      <span className="flex items-center gap-1">
                        <Clock className="h-3 w-3" />
                        Updated {formatRelative(doc.updatedAt)}
                      </span>
                    </div>
                  </div>
                </div>
                <div
                  className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity"
                  onClick={(e) => e.stopPropagation()}
                >
                  <button
                    className="p-1.5 rounded-lg hover:bg-red-50 text-slate-400 hover:text-red-600 transition-colors"
                    onClick={() => {
                      if (confirm("Delete this document?"))
                        deleteMutation.mutate(doc.id);
                    }}
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                  <ArrowRight className="h-4 w-4 text-slate-300 ml-2" />
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      {/* Create modal */}
      <Modal
        open={showCreate}
        onClose={() => setShowCreate(false)}
        title="Create document"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            createMutation.mutate();
          }}
          className="space-y-4"
        >
          <Input
            label="Title"
            placeholder="Document title"
            value={docForm.title}
            onChange={(e) =>
              setDocForm((prev) => ({ ...prev, title: e.target.value }))
            }
            required
            autoFocus
          />
          <Textarea
            label="Content"
            placeholder="Start writing..."
            value={docForm.content}
            onChange={(e) =>
              setDocForm((prev) => ({ ...prev, content: e.target.value }))
            }
            rows={6}
          />
          <div className="flex justify-end gap-3">
            <Button variant="ghost" onClick={() => setShowCreate(false)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              type="submit"
              loading={createMutation.isPending}
            >
              Create
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
