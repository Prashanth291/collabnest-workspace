"use client";

import React, { useState, useRef, useEffect } from "react";
import { useParams } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { useAuthStore } from "@/lib/store";
import { cn, formatRelative, formatTime } from "@/lib/utils";
import {
  Button,
  Card,
  Avatar,
  Skeleton,
  EmptyState,
  Tabs,
} from "@/components/ui";
import type { ChatMessage as ChatMsg, ChatResponse } from "@/lib/types";
import {
  MessageSquare,
  Send,
  Users as UsersIcon,
  User,
  Hash,
} from "lucide-react";

export default function ChatPage() {
  const params = useParams();
  const workspaceId = params.workspaceId as string;
  const { user } = useAuthStore();
  const queryClient = useQueryClient();
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const [activeTab, setActiveTab] = useState("workspace");
  const [activeChatId, setActiveChatId] = useState<string | null>(null);
  const [message, setMessage] = useState("");
  const [selectedDM, setSelectedDM] = useState<ChatResponse | null>(null);

  // Get workspace chat
  const { data: workspaceChat } = useQuery({
    queryKey: ["workspace-chat", workspaceId],
    queryFn: () => api.chat.getWorkspaceChat(workspaceId),
    enabled: !!workspaceId,
  });

  // Get direct chats
  const { data: directChats } = useQuery({
    queryKey: ["direct-chats"],
    queryFn: () => api.chat.listDirectChats(),
  });

  // Determine active chat
  const currentChatId =
    activeTab === "workspace"
      ? workspaceChat?.id
      : selectedDM?.id || null;

  // Get messages
  const { data: messagesPage, isLoading: messagesLoading } = useQuery({
    queryKey: ["chat-messages", currentChatId],
    queryFn: () => api.chat.getMessages(currentChatId!, 0, 100),
    enabled: !!currentChatId,
    refetchInterval: 5000,
  });

  const messages = messagesPage?.content || [];

  // Send message
  const sendMutation = useMutation({
    mutationFn: (content: string) =>
      api.chat.sendMessage(currentChatId!, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["chat-messages", currentChatId],
      });
      setMessage("");
    },
  });

  // Scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // Get workspace members for new DMs
  const { data: members } = useQuery({
    queryKey: ["members", workspaceId],
    queryFn: () => api.workspaces.getMembers(workspaceId),
    enabled: !!workspaceId,
  });

  const startDM = async (userId: string) => {
    try {
      const chat = await api.chat.getDirectChat(userId);
      setSelectedDM(chat);
      setActiveTab("direct");
      queryClient.invalidateQueries({ queryKey: ["direct-chats"] });
    } catch (error) {
      console.error("Failed to start DM:", error);
    }
  };

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!message.trim() || !currentChatId) return;
    sendMutation.mutate(message.trim());
  };

  // Group messages by date
  const groupedMessages: { date: string; messages: ChatMsg[] }[] = [];
  let currentDate = "";
  for (const msg of messages) {
    const msgDate = new Date(msg.createdAt).toLocaleDateString();
    if (msgDate !== currentDate) {
      currentDate = msgDate;
      groupedMessages.push({ date: msgDate, messages: [] });
    }
    groupedMessages[groupedMessages.length - 1].messages.push(msg);
  }

  return (
    <div className="h-[calc(100vh-130px)] flex gap-4">
      {/* Sidebar */}
      <div className="w-64 flex-shrink-0 flex flex-col bg-white rounded-xl border border-slate-200 overflow-hidden">
        <div className="p-3 border-b border-slate-100">
          <Tabs
            value={activeTab}
            onChange={setActiveTab}
            tabs={[
              { value: "workspace", label: "Channel" },
              { value: "direct", label: "Direct" },
            ]}
          />
        </div>

        <div className="flex-1 overflow-y-auto p-2">
          {activeTab === "workspace" ? (
            <button
              onClick={() => setActiveTab("workspace")}
              className={cn(
                "w-full flex items-center gap-3 p-3 rounded-lg text-sm transition-colors",
                "bg-indigo-50 text-indigo-700 font-medium"
              )}
            >
              <Hash className="h-4 w-4" />
              General
            </button>
          ) : (
            <div className="space-y-0.5">
              {directChats?.map((chat: ChatResponse) => (
                <button
                  key={chat.id}
                  onClick={() => setSelectedDM(chat)}
                  className={cn(
                    "w-full flex items-center gap-3 p-3 rounded-lg text-sm transition-colors",
                    selectedDM?.id === chat.id
                      ? "bg-indigo-50 text-indigo-700 font-medium"
                      : "text-slate-600 hover:bg-slate-50"
                  )}
                >
                  <Avatar
                    name={chat.otherUsername || "User"}
                    size="sm"
                    className="!h-6 !w-6 !text-[10px]"
                  />
                  <span className="truncate">
                    {chat.otherUsername || "User"}
                  </span>
                </button>
              ))}

              {/* Start new DM */}
              <div className="pt-2 mt-2 border-t border-slate-100">
                <p className="text-xs text-slate-400 font-medium px-3 mb-1">
                  Members
                </p>
                {members
                  ?.filter((m) => m.userId !== user?.id)
                  .map((m) => (
                    <button
                      key={m.userId}
                      onClick={() => startDM(m.userId)}
                      className="w-full flex items-center gap-3 p-2 rounded-lg text-sm text-slate-500 hover:bg-slate-50 hover:text-slate-700 transition-colors"
                    >
                      <Avatar
                        name={m.name}
                        size="sm"
                        className="!h-5 !w-5 !text-[9px]"
                      />
                      <span className="truncate">{m.name}</span>
                    </button>
                  ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Chat area */}
      <div className="flex-1 flex flex-col bg-white rounded-xl border border-slate-200 overflow-hidden">
        {/* Chat header */}
        <div className="px-5 py-3 border-b border-slate-100 flex items-center gap-3">
          {activeTab === "workspace" ? (
            <>
              <div className="h-8 w-8 rounded-lg bg-indigo-50 text-indigo-600 flex items-center justify-center">
                <Hash className="h-4 w-4" />
              </div>
              <div>
                <h3 className="text-sm font-semibold text-slate-900">
                  General
                </h3>
                <p className="text-xs text-slate-400">Workspace channel</p>
              </div>
            </>
          ) : selectedDM ? (
            <>
              <Avatar
                name={selectedDM.otherUsername || "User"}
                size="sm"
              />
              <div>
                <h3 className="text-sm font-semibold text-slate-900">
                  {selectedDM.otherUsername}
                </h3>
                <p className="text-xs text-slate-400">Direct message</p>
              </div>
            </>
          ) : (
            <p className="text-sm text-slate-400">
              Select a conversation
            </p>
          )}
        </div>

        {/* Messages */}
        <div className="flex-1 overflow-y-auto px-5 py-4">
          {!currentChatId ? (
            <EmptyState
              icon={<MessageSquare className="h-8 w-8" />}
              title="No chat selected"
              description="Select a conversation from the sidebar to start chatting."
            />
          ) : messagesLoading ? (
            <div className="space-y-4">
              {[...Array(3)].map((_, i) => (
                <div key={i} className="flex gap-3">
                  <Skeleton className="h-8 w-8 rounded-full" />
                  <div>
                    <Skeleton className="h-3 w-20 mb-2" />
                    <Skeleton className="h-8 w-56 rounded-xl" />
                  </div>
                </div>
              ))}
            </div>
          ) : messages.length === 0 ? (
            <EmptyState
              icon={<MessageSquare className="h-8 w-8" />}
              title="No messages yet"
              description="Send the first message to start the conversation."
            />
          ) : (
            <div className="space-y-6">
              {groupedMessages.map((group) => (
                <div key={group.date}>
                  {/* Date separator */}
                  <div className="flex items-center gap-3 mb-4">
                    <div className="flex-1 h-px bg-slate-100" />
                    <span className="text-xs text-slate-400 font-medium">
                      {group.date === new Date().toLocaleDateString()
                        ? "Today"
                        : group.date}
                    </span>
                    <div className="flex-1 h-px bg-slate-100" />
                  </div>

                  <div className="space-y-3">
                    {group.messages.map((msg: ChatMsg, i: number) => {
                      const isMe = msg.senderId === user?.id;
                      const showAvatar =
                        i === 0 ||
                        group.messages[i - 1].senderId !== msg.senderId;

                      return (
                        <div
                          key={msg.id}
                          className={cn(
                            "flex gap-3",
                            isMe && "flex-row-reverse"
                          )}
                        >
                          {showAvatar ? (
                            <Avatar
                              name={msg.senderUsername}
                              size="sm"
                              className="!h-7 !w-7 !text-[10px] mt-0.5"
                            />
                          ) : (
                            <div className="w-7" />
                          )}
                          <div
                            className={cn(
                              "max-w-[70%]",
                              isMe && "text-right"
                            )}
                          >
                            {showAvatar && (
                              <div
                                className={cn(
                                  "flex items-center gap-2 mb-0.5",
                                  isMe && "justify-end"
                                )}
                              >
                                <span className="text-xs font-medium text-slate-700">
                                  {isMe ? "You" : msg.senderUsername}
                                </span>
                                <span className="text-[10px] text-slate-400">
                                  {formatTime(msg.createdAt)}
                                </span>
                              </div>
                            )}
                            <div
                              className={cn(
                                "inline-block px-3.5 py-2 rounded-2xl text-sm leading-relaxed",
                                isMe
                                  ? "bg-indigo-600 text-white rounded-br-md"
                                  : "bg-slate-100 text-slate-700 rounded-bl-md"
                              )}
                            >
                              {msg.content}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              ))}
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* Input */}
        {currentChatId && (
          <div className="px-4 py-3 border-t border-slate-100">
            <form onSubmit={handleSend} className="flex gap-2">
              <input
                value={message}
                onChange={(e) => setMessage(e.target.value)}
                placeholder="Type a message..."
                className="flex-1 px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              />
              <Button
                type="submit"
                variant="primary"
                size="icon"
                disabled={!message.trim()}
                loading={sendMutation.isPending}
              >
                <Send className="h-4 w-4" />
              </Button>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}
