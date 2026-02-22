"use client";

import React, { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import {
  Button,
  Card,
  EmptyState,
  Modal,
  Input,
  Skeleton,
  Badge,
} from "@/components/ui";
import { formatRelative } from "@/lib/utils";
import type { Board } from "@/lib/types";
import {
  Plus,
  FolderKanban,
  ArrowRight,
  MoreHorizontal,
  Trash2,
  Edit2,
} from "lucide-react";

export default function BoardsPage() {
  const params = useParams();
  const workspaceId = params.workspaceId as string;
  const router = useRouter();
  const queryClient = useQueryClient();
  const [showCreate, setShowCreate] = useState(false);
  const [editingBoard, setEditingBoard] = useState<Board | null>(null);
  const [boardName, setBoardName] = useState("");

  const { data: boards, isLoading } = useQuery({
    queryKey: ["boards", workspaceId],
    queryFn: () => api.boards.list(workspaceId),
    enabled: !!workspaceId,
  });

  const createMutation = useMutation({
    mutationFn: (name: string) =>
      api.boards.create(workspaceId, {
        name,
        position: (boards?.length || 0) + 1,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["boards", workspaceId] });
      setShowCreate(false);
      setBoardName("");
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (boardId: string) => api.boards.delete(workspaceId, boardId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["boards", workspaceId] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, name, position }: { id: string; name: string; position: number }) =>
      api.boards.update(workspaceId, id, { name, position }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["boards", workspaceId] });
      setEditingBoard(null);
      setBoardName("");
    },
  });

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-xl font-bold text-slate-900">Boards</h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Organize your work with Kanban boards
          </p>
        </div>
        <Button variant="primary" onClick={() => setShowCreate(true)}>
          <Plus className="h-4 w-4" />
          New board
        </Button>
      </div>

      {/* Board list */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[...Array(3)].map((_, i) => (
            <Card key={i} className="p-5">
              <Skeleton className="h-5 w-32 mb-3" />
              <Skeleton className="h-4 w-20" />
            </Card>
          ))}
        </div>
      ) : !boards || boards.length === 0 ? (
        <EmptyState
          icon={<FolderKanban className="h-8 w-8" />}
          title="No boards yet"
          description="Create your first board to start organizing tasks with a Kanban workflow."
          action={
            <Button variant="primary" onClick={() => setShowCreate(true)}>
              <Plus className="h-4 w-4" />
              Create board
            </Button>
          }
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {boards.map((board: Board) => (
            <Card
              key={board.id}
              className="group hover:shadow-md hover:border-indigo-200 transition-all duration-300 cursor-pointer relative"
              onClick={() =>
                router.push(
                  `/workspace/${workspaceId}/board/${board.id}`
                )
              }
            >
              <div className="p-5">
                <div className="flex items-start justify-between mb-3">
                  <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-indigo-500 to-violet-500 flex items-center justify-center text-white">
                    <FolderKanban className="h-5 w-5" />
                  </div>
                  <div
                    className="opacity-0 group-hover:opacity-100 transition-opacity flex gap-1"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <button
                      className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-colors"
                      onClick={() => {
                        setEditingBoard(board);
                        setBoardName(board.name);
                      }}
                    >
                      <Edit2 className="h-3.5 w-3.5" />
                    </button>
                    <button
                      className="p-1.5 rounded-lg hover:bg-red-50 text-slate-400 hover:text-red-600 transition-colors"
                      onClick={() => {
                        if (confirm("Delete this board?"))
                          deleteMutation.mutate(board.id);
                      }}
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </div>
                <h3 className="font-semibold text-slate-900 mb-1 group-hover:text-indigo-700 transition-colors">
                  {board.name}
                </h3>
                <div className="flex items-center justify-between">
                  <span className="text-xs text-slate-400">
                    Created {formatRelative(board.createdAt)}
                  </span>
                  <ArrowRight className="h-4 w-4 text-slate-300 group-hover:text-indigo-500 transition-all" />
                </div>
              </div>
            </Card>
          ))}

          {/* Create new */}
          <button
            onClick={() => setShowCreate(true)}
            className="border-2 border-dashed border-slate-200 hover:border-indigo-300 rounded-xl p-6 flex flex-col items-center justify-center gap-2 text-slate-400 hover:text-indigo-600 transition-all min-h-[140px] group"
          >
            <Plus className="h-6 w-6" />
            <span className="text-sm font-medium">New board</span>
          </button>
        </div>
      )}

      {/* Create modal */}
      <Modal
        open={showCreate}
        onClose={() => setShowCreate(false)}
        title="Create board"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            createMutation.mutate(boardName);
          }}
        >
          <Input
            label="Board name"
            placeholder="e.g. Sprint 1, Product Backlog"
            value={boardName}
            onChange={(e) => setBoardName(e.target.value)}
            required
            autoFocus
          />
          <div className="flex justify-end gap-3 mt-6">
            <Button variant="ghost" onClick={() => setShowCreate(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" loading={createMutation.isPending}>
              Create
            </Button>
          </div>
        </form>
      </Modal>

      {/* Edit modal */}
      <Modal
        open={!!editingBoard}
        onClose={() => setEditingBoard(null)}
        title="Edit board"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (editingBoard)
              updateMutation.mutate({
                id: editingBoard.id,
                name: boardName,
                position: editingBoard.position,
              });
          }}
        >
          <Input
            label="Board name"
            value={boardName}
            onChange={(e) => setBoardName(e.target.value)}
            required
            autoFocus
          />
          <div className="flex justify-end gap-3 mt-6">
            <Button variant="ghost" onClick={() => setEditingBoard(null)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" loading={updateMutation.isPending}>
              Save changes
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
