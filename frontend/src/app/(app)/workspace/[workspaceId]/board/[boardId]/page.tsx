"use client";

import React, { useState, useEffect, useMemo } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  DragDropContext,
  Droppable,
  Draggable,
  type DropResult,
} from "@hello-pangea/dnd";
import api from "@/lib/api";
import { cn, formatRelative } from "@/lib/utils";
import {
  Button,
  Card,
  Modal,
  Input,
  Textarea,
  Select,
  Badge,
  Avatar,
  EmptyState,
  Skeleton,
  Dropdown,
  DropdownItem,
} from "@/components/ui";
import type {
  BoardColumn,
  Task,
  TaskPriority,
  WorkspaceMember,
} from "@/lib/types";
import {
  Plus,
  MoreHorizontal,
  GripVertical,
  Calendar,
  Flag,
  User,
  Trash2,
  Edit2,
  X,
  ArrowRight,
  AlertCircle,
} from "lucide-react";

const priorityConfig: Record<
  TaskPriority,
  { label: string; color: string; bgColor: string }
> = {
  LOW: { label: "Low", color: "text-slate-500", bgColor: "bg-slate-100" },
  MEDIUM: {
    label: "Medium",
    color: "text-blue-600",
    bgColor: "bg-blue-50",
  },
  HIGH: {
    label: "High",
    color: "text-amber-600",
    bgColor: "bg-amber-50",
  },
  CRITICAL: {
    label: "Critical",
    color: "text-red-600",
    bgColor: "bg-red-50",
  },
};

interface ColumnWithTasks extends BoardColumn {
  tasks: Task[];
}

export default function BoardPage() {
  const params = useParams();
  const workspaceId = params.workspaceId as string;
  const boardId = params.boardId as string;
  const queryClient = useQueryClient();

  const [showCreateCol, setShowCreateCol] = useState(false);
  const [colName, setColName] = useState("");
  const [showCreateTask, setShowCreateTask] = useState<string | null>(null);
  const [editingTask, setEditingTask] = useState<{
    task: Task;
    columnId: string;
  } | null>(null);
  const [taskForm, setTaskForm] = useState({
    title: "",
    description: "",
    priority: "MEDIUM" as TaskPriority,
    dueDate: "",
  });

  // Fetch board info
  const { data: board } = useQuery({
    queryKey: ["board", workspaceId, boardId],
    queryFn: () => api.boards.get(workspaceId, boardId),
    enabled: !!workspaceId && !!boardId,
  });

  // Fetch columns
  const { data: columns, isLoading: columnsLoading } = useQuery({
    queryKey: ["columns", workspaceId, boardId],
    queryFn: () => api.columns.list(workspaceId, boardId),
    enabled: !!workspaceId && !!boardId,
  });

  // Fetch members for assignment
  const { data: members } = useQuery({
    queryKey: ["members", workspaceId],
    queryFn: () => api.workspaces.getMembers(workspaceId),
    enabled: !!workspaceId,
  });

  // Fetch tasks for each column
  const { data: allTasks } = useQuery({
    queryKey: ["board-tasks", workspaceId, boardId, columns?.map((c) => c.id)],
    queryFn: async () => {
      if (!columns) return {};
      const results: Record<string, Task[]> = {};
      await Promise.all(
        columns.map(async (col) => {
          try {
            const tasks = await api.tasks.list(workspaceId, col.id);
            results[col.id] = tasks.sort((a, b) => a.position - b.position);
          } catch {
            results[col.id] = [];
          }
        })
      );
      return results;
    },
    enabled: !!columns && columns.length > 0,
  });

  // Build columns with tasks
  const columnsWithTasks: ColumnWithTasks[] = useMemo(() => {
    if (!columns) return [];
    return columns
      .sort((a, b) => a.position - b.position)
      .map((col) => ({
        ...col,
        tasks: allTasks?.[col.id] || [],
      }));
  }, [columns, allTasks]);

  // Create column
  const createColMutation = useMutation({
    mutationFn: (name: string) =>
      api.columns.create(workspaceId, boardId, {
        name,
        position: (columns?.length || 0) + 1,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["columns", workspaceId, boardId],
      });
      setShowCreateCol(false);
      setColName("");
    },
  });

  // Delete column
  const deleteColMutation = useMutation({
    mutationFn: (columnId: string) =>
      api.columns.delete(workspaceId, boardId, columnId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["columns", workspaceId, boardId],
      });
    },
  });

  // Create task
  const createTaskMutation = useMutation({
    mutationFn: ({
      columnId,
      data,
    }: {
      columnId: string;
      data: { title: string; description?: string; priority?: TaskPriority; dueDate?: string };
    }) => api.tasks.create(workspaceId, columnId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["board-tasks", workspaceId, boardId],
      });
      setShowCreateTask(null);
      resetTaskForm();
    },
  });

  // Update task
  const updateTaskMutation = useMutation({
    mutationFn: ({
      columnId,
      taskId,
      data,
    }: {
      columnId: string;
      taskId: string;
      data: { title: string; description?: string; priority?: TaskPriority; dueDate?: string };
    }) => api.tasks.update(workspaceId, columnId, taskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["board-tasks", workspaceId, boardId],
      });
      setEditingTask(null);
      resetTaskForm();
    },
  });

  // Move task
  const moveTaskMutation = useMutation({
    mutationFn: ({
      columnId,
      taskId,
      targetColumnId,
      position,
    }: {
      columnId: string;
      taskId: string;
      targetColumnId: string;
      position: number;
    }) => api.tasks.move(workspaceId, columnId, taskId, { targetColumnId, position }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["board-tasks", workspaceId, boardId],
      });
    },
  });

  // Delete task
  const deleteTaskMutation = useMutation({
    mutationFn: ({ columnId, taskId }: { columnId: string; taskId: string }) =>
      api.tasks.delete(workspaceId, columnId, taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["board-tasks", workspaceId, boardId],
      });
    },
  });

  // Assign task
  const assignTaskMutation = useMutation({
    mutationFn: ({
      columnId,
      taskId,
      userId,
    }: {
      columnId: string;
      taskId: string;
      userId: string | null;
    }) => api.tasks.assign(workspaceId, columnId, taskId, { userId }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["board-tasks", workspaceId, boardId],
      });
    },
  });

  const resetTaskForm = () => {
    setTaskForm({ title: "", description: "", priority: "MEDIUM", dueDate: "" });
  };

  // Drag and drop handler
  const onDragEnd = (result: DropResult) => {
    const { source, destination, draggableId } = result;
    if (!destination) return;
    if (
      source.droppableId === destination.droppableId &&
      source.index === destination.index
    )
      return;

    moveTaskMutation.mutate({
      columnId: source.droppableId,
      taskId: draggableId,
      targetColumnId: destination.droppableId,
      position: destination.index,
    });
  };

  if (columnsLoading) {
    return (
      <div className="flex gap-5 overflow-x-auto pb-4">
        {[...Array(3)].map((_, i) => (
          <div
            key={i}
            className="flex-shrink-0 w-80 bg-slate-50 rounded-xl p-4"
          >
            <Skeleton className="h-5 w-24 mb-4" />
            <Skeleton className="h-24 w-full mb-3" />
            <Skeleton className="h-24 w-full mb-3" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="h-[calc(100vh-130px)]">
      {/* Header */}
      <div className="flex items-center justify-between mb-5">
        <div>
          <h1 className="text-xl font-bold text-slate-900">
            {board?.name || "Board"}
          </h1>
          <p className="text-sm text-slate-500 mt-0.5">
            {columnsWithTasks.length} columns &middot;{" "}
            {columnsWithTasks.reduce((acc, col) => acc + col.tasks.length, 0)}{" "}
            tasks
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={() => setShowCreateCol(true)}>
          <Plus className="h-3.5 w-3.5" />
          Add column
        </Button>
      </div>

      {/* Board */}
      <DragDropContext onDragEnd={onDragEnd}>
        <div className="flex gap-4 overflow-x-auto pb-4 h-full">
          {columnsWithTasks.map((column) => (
            <div
              key={column.id}
              className="flex-shrink-0 w-80 flex flex-col bg-slate-50/80 rounded-xl border border-slate-100"
            >
              {/* Column header */}
              <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100">
                <div className="flex items-center gap-2">
                  <h3 className="font-semibold text-sm text-slate-700">
                    {column.name}
                  </h3>
                  <span className="text-xs text-slate-400 bg-white px-1.5 py-0.5 rounded-md">
                    {column.tasks.length}
                  </span>
                </div>
                <div className="flex items-center gap-1">
                  <button
                    onClick={() => {
                      setShowCreateTask(column.id);
                      resetTaskForm();
                    }}
                    className="p-1 rounded-md text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 transition-colors"
                  >
                    <Plus className="h-4 w-4" />
                  </button>
                  <Dropdown
                    trigger={
                      <button className="p-1 rounded-md text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors">
                        <MoreHorizontal className="h-4 w-4" />
                      </button>
                    }
                  >
                    <DropdownItem
                      onClick={() => {
                        if (confirm("Delete this column and all its tasks?"))
                          deleteColMutation.mutate(column.id);
                      }}
                      className="text-red-600"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                      Delete column
                    </DropdownItem>
                  </Dropdown>
                </div>
              </div>

              {/* Tasks */}
              <Droppable droppableId={column.id}>
                {(provided, snapshot) => (
                  <div
                    ref={provided.innerRef}
                    {...provided.droppableProps}
                    className={cn(
                      "flex-1 overflow-y-auto px-3 py-2 space-y-2 min-h-[60px] transition-colors",
                      snapshot.isDraggingOver && "bg-indigo-50/50"
                    )}
                  >
                    {column.tasks.map((task, index) => (
                      <Draggable
                        key={task.id}
                        draggableId={task.id}
                        index={index}
                      >
                        {(provided, snapshot) => (
                          <div
                            ref={provided.innerRef}
                            {...provided.draggableProps}
                            className={cn(
                              "bg-white rounded-lg border border-slate-200 p-3 shadow-sm hover:shadow-md transition-all group",
                              snapshot.isDragging && "shadow-lg rotate-2 border-indigo-200"
                            )}
                          >
                            <div className="flex items-start gap-2">
                              <div
                                {...provided.dragHandleProps}
                                className="mt-0.5 text-slate-300 hover:text-slate-500 opacity-0 group-hover:opacity-100 transition-opacity cursor-grab"
                              >
                                <GripVertical className="h-4 w-4" />
                              </div>
                              <div className="flex-1 min-w-0">
                                <div className="flex items-start justify-between gap-2">
                                  <h4 className="text-sm font-medium text-slate-900 leading-snug">
                                    {task.title}
                                  </h4>
                                  <div className="opacity-0 group-hover:opacity-100 transition-opacity flex items-center">
                                    <button
                                      onClick={() => {
                                        setEditingTask({
                                          task,
                                          columnId: column.id,
                                        });
                                        setTaskForm({
                                          title: task.title,
                                          description: task.description || "",
                                          priority: task.priority,
                                          dueDate: task.dueDate || "",
                                        });
                                      }}
                                      className="p-1 rounded text-slate-400 hover:text-slate-600 hover:bg-slate-50"
                                    >
                                      <Edit2 className="h-3 w-3" />
                                    </button>
                                  </div>
                                </div>

                                {task.description && (
                                  <p className="text-xs text-slate-500 mt-1 line-clamp-2">
                                    {task.description}
                                  </p>
                                )}

                                {/* Tags row */}
                                <div className="flex flex-wrap items-center gap-1.5 mt-2">
                                  {/* Priority */}
                                  <span
                                    className={cn(
                                      "inline-flex items-center gap-1 text-[10px] font-medium px-1.5 py-0.5 rounded",
                                      priorityConfig[task.priority].bgColor,
                                      priorityConfig[task.priority].color
                                    )}
                                  >
                                    <Flag className="h-2.5 w-2.5" />
                                    {priorityConfig[task.priority].label}
                                  </span>

                                  {/* Due date */}
                                  {task.dueDate && (
                                    <span className="inline-flex items-center gap-1 text-[10px] font-medium text-slate-500 bg-slate-50 px-1.5 py-0.5 rounded">
                                      <Calendar className="h-2.5 w-2.5" />
                                      {new Date(task.dueDate).toLocaleDateString(
                                        "en-US",
                                        {
                                          month: "short",
                                          day: "numeric",
                                        }
                                      )}
                                    </span>
                                  )}

                                  {/* Spacer + Assignee */}
                                  <div className="flex-1" />
                                  {task.assigneeName ? (
                                    <Avatar
                                      name={task.assigneeName}
                                      size="sm"
                                      className="!h-5 !w-5 !text-[9px] !ring-1"
                                    />
                                  ) : (
                                    <Dropdown
                                      trigger={
                                        <button className="p-0.5 rounded text-slate-300 hover:text-indigo-500 hover:bg-indigo-50 opacity-0 group-hover:opacity-100 transition-all">
                                          <User className="h-3.5 w-3.5" />
                                        </button>
                                      }
                                    >
                                      {members?.map((m) => (
                                        <DropdownItem
                                          key={m.userId}
                                          onClick={() =>
                                            assignTaskMutation.mutate({
                                              columnId: column.id,
                                              taskId: task.id,
                                              userId: m.userId,
                                            })
                                          }
                                        >
                                          <Avatar
                                            name={m.name}
                                            size="sm"
                                            className="!h-5 !w-5 !text-[9px]"
                                          />
                                          {m.name}
                                        </DropdownItem>
                                      ))}
                                    </Dropdown>
                                  )}
                                </div>
                              </div>
                            </div>
                          </div>
                        )}
                      </Draggable>
                    ))}
                    {provided.placeholder}
                  </div>
                )}
              </Droppable>

              {/* Add task button at bottom */}
              <div className="px-3 pb-3">
                <button
                  onClick={() => {
                    setShowCreateTask(column.id);
                    resetTaskForm();
                  }}
                  className="w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm text-slate-400 hover:text-indigo-600 hover:bg-indigo-50/50 transition-colors"
                >
                  <Plus className="h-3.5 w-3.5" />
                  Add task
                </button>
              </div>
            </div>
          ))}

          {/* Add column */}
          <button
            onClick={() => setShowCreateCol(true)}
            className="flex-shrink-0 w-72 h-fit border-2 border-dashed border-slate-200 hover:border-indigo-300 rounded-xl p-8 flex flex-col items-center justify-center gap-2 text-slate-400 hover:text-indigo-600 transition-all"
          >
            <Plus className="h-5 w-5" />
            <span className="text-sm font-medium">Add column</span>
          </button>
        </div>
      </DragDropContext>

      {/* Create column modal */}
      <Modal
        open={showCreateCol}
        onClose={() => setShowCreateCol(false)}
        title="Add column"
        size="sm"
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            createColMutation.mutate(colName);
          }}
        >
          <Input
            label="Column name"
            placeholder="e.g. To Do, In Progress, Done"
            value={colName}
            onChange={(e) => setColName(e.target.value)}
            required
            autoFocus
          />
          <div className="flex justify-end gap-3 mt-6">
            <Button variant="ghost" onClick={() => setShowCreateCol(false)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              type="submit"
              loading={createColMutation.isPending}
            >
              Add column
            </Button>
          </div>
        </form>
      </Modal>

      {/* Create / Edit task modal */}
      <Modal
        open={!!showCreateTask || !!editingTask}
        onClose={() => {
          setShowCreateTask(null);
          setEditingTask(null);
          resetTaskForm();
        }}
        title={editingTask ? "Edit task" : "Create task"}
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            const data = {
              title: taskForm.title,
              description: taskForm.description || undefined,
              priority: taskForm.priority,
              dueDate: taskForm.dueDate || undefined,
            };
            if (editingTask) {
              updateTaskMutation.mutate({
                columnId: editingTask.columnId,
                taskId: editingTask.task.id,
                data,
              });
            } else if (showCreateTask) {
              createTaskMutation.mutate({ columnId: showCreateTask, data });
            }
          }}
          className="space-y-4"
        >
          <Input
            label="Title"
            placeholder="What needs to be done?"
            value={taskForm.title}
            onChange={(e) =>
              setTaskForm((prev) => ({ ...prev, title: e.target.value }))
            }
            required
            autoFocus
          />
          <Textarea
            label="Description"
            placeholder="Add more details..."
            value={taskForm.description}
            onChange={(e) =>
              setTaskForm((prev) => ({ ...prev, description: e.target.value }))
            }
            rows={3}
          />
          <div className="grid grid-cols-2 gap-3">
            <Select
              label="Priority"
              value={taskForm.priority}
              onChange={(e) =>
                setTaskForm((prev) => ({
                  ...prev,
                  priority: e.target.value as TaskPriority,
                }))
              }
              options={[
                { value: "LOW", label: "Low" },
                { value: "MEDIUM", label: "Medium" },
                { value: "HIGH", label: "High" },
                { value: "CRITICAL", label: "Critical" },
              ]}
            />
            <Input
              label="Due date"
              type="date"
              value={taskForm.dueDate}
              onChange={(e) =>
                setTaskForm((prev) => ({ ...prev, dueDate: e.target.value }))
              }
            />
          </div>

          {editingTask && (
            <div className="flex items-center gap-2 pt-2 border-t border-slate-100">
              <button
                type="button"
                onClick={() => {
                  if (confirm("Delete this task?")) {
                    deleteTaskMutation.mutate({
                      columnId: editingTask.columnId,
                      taskId: editingTask.task.id,
                    });
                    setEditingTask(null);
                  }
                }}
                className="text-sm text-red-600 hover:text-red-700 flex items-center gap-1"
              >
                <Trash2 className="h-3.5 w-3.5" />
                Delete task
              </button>
            </div>
          )}

          <div className="flex justify-end gap-3 pt-2">
            <Button
              variant="ghost"
              type="button"
              onClick={() => {
                setShowCreateTask(null);
                setEditingTask(null);
                resetTaskForm();
              }}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              type="submit"
              loading={
                createTaskMutation.isPending || updateTaskMutation.isPending
              }
            >
              {editingTask ? "Save changes" : "Create task"}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
