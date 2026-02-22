"use client";

import React, { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { useAuthStore } from "@/lib/store";
import { cn, formatRelative } from "@/lib/utils";
import {
  Button,
  Card,
  Textarea,
  Avatar,
  Skeleton,
} from "@/components/ui";
import type { Comment } from "@/lib/types";
import {
  ArrowLeft,
  Save,
  MessageSquare,
  Send,
  Trash2,
  Clock,
} from "lucide-react";

export default function DocumentDetailPage() {
  const params = useParams();
  const workspaceId = params.workspaceId as string;
  const documentId = params.documentId as string;
  const router = useRouter();
  const { user } = useAuthStore();
  const queryClient = useQueryClient();
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState({ title: "", content: "" });
  const [newComment, setNewComment] = useState("");

  const { data: doc, isLoading } = useQuery({
    queryKey: ["document", workspaceId, documentId],
    queryFn: () => api.documents.get(workspaceId, documentId),
    enabled: !!workspaceId && !!documentId,
  });

  const { data: comments } = useQuery({
    queryKey: ["comments", workspaceId, documentId],
    queryFn: () => api.documents.getComments(workspaceId, documentId),
    enabled: !!workspaceId && !!documentId,
  });

  const updateMutation = useMutation({
    mutationFn: () =>
      api.documents.update(workspaceId, documentId, editForm),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["document", workspaceId, documentId],
      });
      setIsEditing(false);
    },
  });

  const addCommentMutation = useMutation({
    mutationFn: (content: string) =>
      api.documents.addComment(workspaceId, documentId, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["comments", workspaceId, documentId],
      });
      setNewComment("");
    },
  });

  const deleteCommentMutation = useMutation({
    mutationFn: (commentId: string) =>
      api.documents.deleteComment(workspaceId, documentId, commentId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["comments", workspaceId, documentId],
      });
    },
  });

  const startEditing = () => {
    if (doc) {
      setEditForm({ title: doc.title, content: doc.content });
      setIsEditing(true);
    }
  };

  if (isLoading) {
    return (
      <div className="max-w-4xl mx-auto">
        <Skeleton className="h-8 w-64 mb-4" />
        <Skeleton className="h-4 w-32 mb-8" />
        <Skeleton className="h-64 w-full" />
      </div>
    );
  }

  if (!doc) return null;

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() =>
            router.push(`/workspace/${workspaceId}/documents`)
          }
          className="p-2 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors"
        >
          <ArrowLeft className="h-4 w-4" />
        </button>
        <div className="flex-1">
          {isEditing ? (
            <input
              value={editForm.title}
              onChange={(e) =>
                setEditForm((prev) => ({ ...prev, title: e.target.value }))
              }
              className="text-xl font-bold text-slate-900 bg-transparent border-none outline-none w-full"
              autoFocus
            />
          ) : (
            <h1 className="text-xl font-bold text-slate-900">{doc.title}</h1>
          )}
          <p className="text-xs text-slate-400 mt-0.5 flex items-center gap-1">
            <Clock className="h-3 w-3" />
            Updated {formatRelative(doc.updatedAt)}
          </p>
        </div>
        {isEditing ? (
          <div className="flex gap-2">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => setIsEditing(false)}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              size="sm"
              onClick={() => updateMutation.mutate()}
              loading={updateMutation.isPending}
            >
              <Save className="h-3.5 w-3.5" />
              Save
            </Button>
          </div>
        ) : (
          <Button variant="outline" size="sm" onClick={startEditing}>
            Edit
          </Button>
        )}
      </div>

      {/* Content */}
      <Card className="mb-8">
        <div className="p-6">
          {isEditing ? (
            <textarea
              value={editForm.content}
              onChange={(e) =>
                setEditForm((prev) => ({ ...prev, content: e.target.value }))
              }
              className="w-full min-h-[400px] text-sm text-slate-700 leading-relaxed bg-transparent border-none outline-none resize-y"
              placeholder="Write your document content here..."
            />
          ) : (
            <div className="prose prose-slate max-w-none">
              <div className="text-sm text-slate-700 leading-relaxed whitespace-pre-wrap">
                {doc.content || (
                  <span className="text-slate-400 italic">
                    No content yet. Click Edit to start writing.
                  </span>
                )}
              </div>
            </div>
          )}
        </div>
      </Card>

      {/* Comments */}
      <div>
        <div className="flex items-center gap-2 mb-4">
          <MessageSquare className="h-4 w-4 text-slate-500" />
          <h2 className="font-semibold text-slate-900">
            Comments ({comments?.length || 0})
          </h2>
        </div>

        {/* New comment */}
        <Card className="mb-4">
          <div className="p-4">
            <div className="flex gap-3">
              {user && <Avatar name={user.name} size="sm" />}
              <div className="flex-1">
                <textarea
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  placeholder="Write a comment..."
                  className="w-full text-sm text-slate-700 bg-transparent border-none outline-none resize-none min-h-[60px]"
                />
                <div className="flex justify-end mt-2">
                  <Button
                    variant="primary"
                    size="sm"
                    disabled={!newComment.trim()}
                    onClick={() => addCommentMutation.mutate(newComment)}
                    loading={addCommentMutation.isPending}
                  >
                    <Send className="h-3.5 w-3.5" />
                    Comment
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </Card>

        {/* Comment list */}
        <div className="space-y-3">
          {comments?.map((comment: Comment) => (
            <Card key={comment.id}>
              <div className="p-4">
                <div className="flex items-start gap-3">
                  <Avatar name={comment.createdByName} size="sm" />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-sm font-medium text-slate-900">
                        {comment.createdByName}
                      </span>
                      <span className="text-xs text-slate-400">
                        {formatRelative(comment.createdAt)}
                      </span>
                    </div>
                    <p className="text-sm text-slate-600 leading-relaxed">
                      {comment.content}
                    </p>
                  </div>
                  {comment.createdById === user?.id && (
                    <button
                      onClick={() => {
                        if (confirm("Delete this comment?"))
                          deleteCommentMutation.mutate(comment.id);
                      }}
                      className="p-1 rounded text-slate-300 hover:text-red-500 transition-colors"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}
