"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useAuthStore, useWorkspaceStore } from "@/lib/store";
import { cn } from "@/lib/utils";
import { Avatar, Button, Dropdown, DropdownItem, Badge } from "@/components/ui";
import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import {
  LayoutDashboard,
  FolderKanban,
  FileText,
  MessageSquare,
  Users,
  Bell,
  Settings,
  LogOut,
  Plus,
  ChevronDown,
  Activity,
  HardDrive,
  Search,
  Menu,
  X,
} from "lucide-react";

interface NavItem {
  label: string;
  href: string;
  icon: React.ReactNode;
  badge?: number;
}

export default function AppLayout({ children }: { children: React.ReactNode }) {
  const { user, isAuthenticated, logout } = useAuthStore();
  const { workspaces, currentWorkspace, setCurrentWorkspace, fetchWorkspaces } =
    useWorkspaceStore();
  const router = useRouter();
  const pathname = usePathname();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  // Redirect if not authenticated
  useEffect(() => {
    if (!isAuthenticated && !useAuthStore.getState().isLoading) {
      router.push("/login");
    }
  }, [isAuthenticated, router]);

  // Load workspaces
  useEffect(() => {
    if (isAuthenticated) {
      fetchWorkspaces();
    }
  }, [isAuthenticated, fetchWorkspaces]);

  // Set current workspace from URL
  useEffect(() => {
    const match = pathname.match(/\/workspace\/([^/]+)/);
    if (match && workspaces.length > 0) {
      const ws = workspaces.find((w) => w.id === match[1]);
      if (ws) setCurrentWorkspace(ws);
    }
  }, [pathname, workspaces, setCurrentWorkspace]);

  // Notification stats
  const { data: notifStats } = useQuery({
    queryKey: ["notification-stats"],
    queryFn: () => api.notifications.getStats(),
    enabled: isAuthenticated,
    refetchInterval: 30_000,
  });

  if (!isAuthenticated || !user) return null;

  const workspaceId = currentWorkspace?.id;
  const isInWorkspace = pathname.startsWith("/workspace/");

  const mainNav: NavItem[] = [
    {
      label: "Dashboard",
      href: "/dashboard",
      icon: <LayoutDashboard className="h-4.5 w-4.5" />,
    },
  ];

  const workspaceNav: NavItem[] = workspaceId
    ? [
        {
          label: "Boards",
          href: `/workspace/${workspaceId}/boards`,
          icon: <FolderKanban className="h-4.5 w-4.5" />,
        },
        {
          label: "Documents",
          href: `/workspace/${workspaceId}/documents`,
          icon: <FileText className="h-4.5 w-4.5" />,
        },
        {
          label: "Chat",
          href: `/workspace/${workspaceId}/chat`,
          icon: <MessageSquare className="h-4.5 w-4.5" />,
        },
        {
          label: "Files",
          href: `/workspace/${workspaceId}/files`,
          icon: <HardDrive className="h-4.5 w-4.5" />,
        },
        {
          label: "Members",
          href: `/workspace/${workspaceId}/members`,
          icon: <Users className="h-4.5 w-4.5" />,
        },
        {
          label: "Activity",
          href: `/workspace/${workspaceId}/activity`,
          icon: <Activity className="h-4.5 w-4.5" />,
        },
        {
          label: "Settings",
          href: `/workspace/${workspaceId}/settings`,
          icon: <Settings className="h-4.5 w-4.5" />,
        },
      ]
    : [];

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  function NavLink({ item }: { item: NavItem }) {
    const isActive =
      pathname === item.href || pathname.startsWith(item.href + "/");
    return (
      <Link
        href={item.href}
        onClick={() => setSidebarOpen(false)}
        className={cn(
          "flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-150",
          isActive
            ? "bg-indigo-50 text-indigo-700"
            : "text-slate-600 hover:text-slate-900 hover:bg-slate-50"
        )}
      >
        <span
          className={cn(
            "transition-colors",
            isActive ? "text-indigo-600" : "text-slate-400"
          )}
        >
          {item.icon}
        </span>
        {item.label}
        {item.badge !== undefined && item.badge > 0 && (
          <Badge variant="primary" className="ml-auto">
            {item.badge}
          </Badge>
        )}
      </Link>
    );
  }

  const sidebarContent = (
    <>
      {/* Logo */}
      <div className="px-4 h-16 flex items-center justify-between border-b border-slate-100">
        <Link
          href="/dashboard"
          className="flex items-center gap-2.5"
          onClick={() => setSidebarOpen(false)}
        >
          <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-indigo-600 to-violet-600 flex items-center justify-center">
            <span className="text-white font-bold text-sm">CN</span>
          </div>
          <span className="font-bold text-lg text-slate-900 tracking-tight">
            CollabNest
          </span>
        </Link>
        <button
          className="lg:hidden p-1 text-slate-400 hover:text-slate-600"
          onClick={() => setSidebarOpen(false)}
        >
          <X className="h-5 w-5" />
        </button>
      </div>

      {/* Nav */}
      <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
        {/* Main nav */}
        <div className="space-y-1">
          {mainNav.map((item) => (
            <NavLink key={item.href} item={item} />
          ))}
        </div>

        {/* Workspace selector */}
        {workspaces.length > 0 && (
          <div>
            <div className="flex items-center justify-between px-3 mb-2">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                Workspaces
              </span>
              <Link
                href="/dashboard"
                onClick={() => setSidebarOpen(false)}
                className="text-slate-400 hover:text-indigo-600 transition-colors"
              >
                <Plus className="h-3.5 w-3.5" />
              </Link>
            </div>
            <div className="space-y-0.5">
              {workspaces.map((ws) => (
                <Link
                  key={ws.id}
                  href={`/workspace/${ws.id}/boards`}
                  onClick={() => {
                    setCurrentWorkspace(ws);
                    setSidebarOpen(false);
                  }}
                  className={cn(
                    "flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-all duration-150",
                    currentWorkspace?.id === ws.id && isInWorkspace
                      ? "bg-indigo-50 text-indigo-700 font-medium"
                      : "text-slate-600 hover:text-slate-900 hover:bg-slate-50"
                  )}
                >
                  <div
                    className={cn(
                      "h-7 w-7 rounded-md flex items-center justify-center text-xs font-bold",
                      currentWorkspace?.id === ws.id && isInWorkspace
                        ? "bg-indigo-100 text-indigo-700"
                        : "bg-slate-100 text-slate-500"
                    )}
                  >
                    {ws.name.charAt(0).toUpperCase()}
                  </div>
                  <span className="truncate">{ws.name}</span>
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* Workspace sub-nav */}
        {isInWorkspace && workspaceNav.length > 0 && (
          <div>
            <div className="px-3 mb-2">
              <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
                {currentWorkspace?.name || "Workspace"}
              </span>
            </div>
            <div className="space-y-0.5">
              {workspaceNav.map((item) => (
                <NavLink key={item.href} item={item} />
              ))}
            </div>
          </div>
        )}
      </div>

      {/* User */}
      <div className="border-t border-slate-100 p-3">
        <Dropdown
          align="left"
          trigger={
            <button className="flex items-center gap-3 w-full px-3 py-2 rounded-lg hover:bg-slate-50 transition-colors">
              <Avatar name={user.name} size="sm" />
              <div className="flex-1 text-left min-w-0">
                <p className="text-sm font-medium text-slate-900 truncate">
                  {user.name}
                </p>
                <p className="text-xs text-slate-500 truncate">
                  @{user.username}
                </p>
              </div>
              <ChevronDown className="h-4 w-4 text-slate-400" />
            </button>
          }
        >
          <DropdownItem onClick={() => router.push("/profile")}>
            <Settings className="h-4 w-4 text-slate-400" />
            Profile settings
          </DropdownItem>
          <DropdownItem onClick={handleLogout} className="text-red-600">
            <LogOut className="h-4 w-4" />
            Sign out
          </DropdownItem>
        </Dropdown>
      </div>
    </>
  );

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/30 backdrop-blur-sm z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          "fixed top-0 left-0 bottom-0 w-64 bg-white border-r border-slate-200 z-50 flex flex-col transition-transform duration-300 lg:translate-x-0",
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        )}
      >
        {sidebarContent}
      </aside>

      {/* Main area */}
      <div className="lg:pl-64">
        {/* Top bar */}
        <header className="sticky top-0 z-30 bg-white/80 backdrop-blur-xl border-b border-slate-100">
          <div className="flex items-center justify-between h-14 px-4 sm:px-6">
            <div className="flex items-center gap-3">
              <button
                className="lg:hidden p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-50 rounded-lg"
                onClick={() => setSidebarOpen(true)}
              >
                <Menu className="h-5 w-5" />
              </button>

              {/* Breadcrumb-like workspace indicator */}
              {isInWorkspace && currentWorkspace && (
                <div className="hidden sm:flex items-center gap-2 text-sm">
                  <span className="text-slate-400">Workspace</span>
                  <span className="text-slate-300">/</span>
                  <Dropdown
                    align="left"
                    trigger={
                      <button className="flex items-center gap-1.5 font-medium text-slate-700 hover:text-slate-900 transition-colors">
                        {currentWorkspace.name}
                        <ChevronDown className="h-3.5 w-3.5 text-slate-400" />
                      </button>
                    }
                  >
                    {workspaces.map((ws) => (
                      <DropdownItem
                        key={ws.id}
                        onClick={() => {
                          setCurrentWorkspace(ws);
                          router.push(`/workspace/${ws.id}/boards`);
                        }}
                      >
                        <div
                          className={cn(
                            "h-5 w-5 rounded text-xs font-bold flex items-center justify-center",
                            ws.id === currentWorkspace.id
                              ? "bg-indigo-100 text-indigo-600"
                              : "bg-slate-100 text-slate-500"
                          )}
                        >
                          {ws.name.charAt(0)}
                        </div>
                        {ws.name}
                      </DropdownItem>
                    ))}
                  </Dropdown>
                </div>
              )}
            </div>

            <div className="flex items-center gap-2">
              {/* Search */}
              <button className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-50 rounded-lg transition-colors">
                <Search className="h-4.5 w-4.5" />
              </button>

              {/* Notifications */}
              <Link
                href="/notifications"
                className="relative p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-50 rounded-lg transition-colors"
              >
                <Bell className="h-4.5 w-4.5" />
                {notifStats && notifStats.unreadCount > 0 && (
                  <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-red-500 ring-2 ring-white" />
                )}
              </Link>

              {/* Avatar */}
              <div className="hidden sm:block">
                <Avatar name={user.name} size="sm" />
              </div>
            </div>
          </div>
        </header>

        {/* Content */}
        <main className="p-4 sm:p-6">{children}</main>
      </div>
    </div>
  );
}
