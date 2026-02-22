"use client";
import React from "react";
import Link from "next/link";
import { useAuthStore } from "@/lib/store";
import { useRouter } from "next/navigation";
import {
  ArrowRight,
  CheckCircle2,
  LayoutDashboard,
  MessageSquare,
  FileText,
  Users,
  Zap,
  Shield,
} from "lucide-react";
import { Button } from "@/components/ui";

export default function LandingPage() {
  const { isAuthenticated } = useAuthStore();
  const router = useRouter();

  React.useEffect(() => {
    if (isAuthenticated) router.push("/dashboard");
  }, [isAuthenticated, router]);

  return (
    <div className="min-h-screen bg-white text-slate-900">
      {/* ───────── Navbar ───────── */}
      <nav className="fixed top-0 inset-x-0 z-50 flex items-center justify-between px-6 py-4 bg-white/80 backdrop-blur border-b border-slate-100">
        <Link href="/" className="flex items-center gap-2 font-bold text-lg">
          <span className="w-8 h-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center text-sm font-bold">
            CN
          </span>
          CollabNest
        </Link>
        <div className="flex items-center gap-3">
          <Link
            href="/login"
            className="text-sm text-slate-600 hover:text-slate-900 transition-colors"
          >
            Log in
          </Link>
          <Link
            href="/register"
            className="flex items-center gap-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
          >
            Get started <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </nav>

      {/* ───────── Hero ───────── */}
      <section className="relative pt-32 pb-20 px-6 overflow-hidden">
        {/* Background gradient */}
        <div className="absolute inset-0 bg-gradient-to-b from-indigo-50/60 via-white to-white pointer-events-none" />

        <div className="relative max-w-4xl mx-auto flex flex-col items-center text-center">
          {/* Badge */}
          <span className="landing-fade-up landing-delay-1 inline-flex items-center gap-1.5 bg-indigo-50 text-indigo-700 text-xs font-semibold px-3 py-1.5 rounded-full border border-indigo-100 mb-6">
            <Zap className="w-3 h-3" /> Now in public beta
          </span>

          {/* Headline */}
          <h1 className="landing-fade-up landing-delay-2 text-5xl sm:text-6xl md:text-7xl font-extrabold tracking-tight leading-[1.08] text-center">
            The workspace where{" "}
            <span className="text-indigo-600">teams ship faster</span>
          </h1>

          {/* Subtitle */}
          <p className="landing-fade-up landing-delay-3 mt-6 text-lg sm:text-xl text-slate-500 max-w-2xl text-center leading-relaxed">
            Boards, tasks, docs, and chat in one place. Stop juggling tools —
            start building together.
          </p>

          {/* CTA */}
          <div className="landing-fade-up landing-delay-4 mt-8 flex flex-wrap items-center justify-center gap-3">
            <Link
              href="/register"
              className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-6 py-3 rounded-xl transition-colors shadow-lg shadow-indigo-200"
            >
              Start for free <ArrowRight className="w-4 h-4" />
            </Link>
            <Link
              href="/login"
              className="flex items-center gap-2 border border-slate-200 hover:border-slate-300 text-slate-700 font-semibold px-6 py-3 rounded-xl transition-colors bg-white"
            >
              Sign in to workspace
            </Link>
          </div>

          {/* Trust bar */}
          <div className="landing-fade-up landing-delay-4 mt-5 flex flex-wrap items-center justify-center gap-x-6 gap-y-2">
            {[
              "Free forever for small teams",
              "No credit card required",
              "Setup in 60 seconds",
            ].map((t) => (
              <span
                key={t}
                className="flex items-center gap-1.5 text-sm text-slate-500"
              >
                <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0" />
                {t}
              </span>
            ))}
          </div>
        </div>

        {/* ── Product screenshot / mock ── */}
        <div className="landing-fade-up landing-delay-4 relative mt-14 max-w-5xl mx-auto">
          {/* Browser chrome */}
          <div className="rounded-2xl border border-slate-200 shadow-2xl shadow-slate-200/70 overflow-hidden bg-white">
            <div className="flex items-center gap-2 px-4 py-3 bg-slate-50 border-b border-slate-200">
              <span className="w-3 h-3 rounded-full bg-red-400" />
              <span className="w-3 h-3 rounded-full bg-amber-400" />
              <span className="w-3 h-3 rounded-full bg-emerald-400" />
              <span className="flex-1 text-center text-xs text-slate-400 font-mono">
                app.collabnest.io/workspace/boards
              </span>
              <span className="w-14" />
            </div>

            {/* App mock - sidebar + kanban */}
            <div className="flex h-[420px] overflow-hidden">
              {/* Sidebar */}
              <div className="w-44 shrink-0 bg-slate-50 border-r border-slate-200 p-3 flex flex-col gap-1">
                <div className="flex items-center gap-2 px-2 py-2 mb-2">
                  <span className="w-7 h-7 rounded-lg bg-indigo-600 text-white flex items-center justify-center text-xs font-bold shrink-0">
                    CN
                  </span>
                  <div>
                    <p className="text-xs font-semibold text-slate-800 leading-none">
                      Design Team
                    </p>
                    <p className="text-[10px] text-slate-400 mt-0.5">
                      5 members
                    </p>
                  </div>
                </div>
                {[
                  { label: "Boards", active: true },
                  { label: "Documents", active: false },
                  { label: "Chat", active: false },
                  { label: "Members", active: false },
                  { label: "Activity", active: false },
                ].map((nav) => (
                  <div
                    key={nav.label}
                    className={`text-xs px-3 py-2 rounded-lg cursor-pointer transition-colors ${
                      nav.active
                        ? "bg-indigo-50 text-indigo-700 font-semibold"
                        : "text-slate-500 hover:bg-slate-100"
                    }`}
                  >
                    {nav.label}
                  </div>
                ))}
              </div>

              {/* Main kanban area */}
              <div className="flex-1 p-4 overflow-auto bg-white">
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-sm font-bold text-slate-800">
                    Sprint 14
                  </h3>
                  <div className="flex items-center gap-2">
                    <button className="text-xs text-slate-500 hover:text-slate-700">
                      Filter
                    </button>
                    <button className="text-xs bg-indigo-600 text-white px-2.5 py-1 rounded-md font-medium">
                      + Add task
                    </button>
                  </div>
                </div>

                <div className="grid grid-cols-3 gap-3 min-w-[540px]">
                  {[
                    {
                      name: "To Do",
                      count: 3,
                      dot: "bg-slate-400",
                      cards: [
                        {
                          title: "Design system tokens",
                          tag: "Design",
                          tagColor: "bg-violet-100 text-violet-700",
                          priority: "bg-amber-400",
                        },
                        {
                          title: "API error handling",
                          tag: "Backend",
                          tagColor: "bg-blue-100 text-blue-700",
                          priority: "bg-red-400",
                        },
                        {
                          title: "User onboarding flow",
                          tag: "Product",
                          tagColor: "bg-emerald-100 text-emerald-700",
                          priority: "bg-slate-300",
                        },
                      ],
                    },
                    {
                      name: "In Progress",
                      count: 2,
                      dot: "bg-indigo-500",
                      cards: [
                        {
                          title: "Board drag & drop",
                          tag: "Frontend",
                          tagColor: "bg-orange-100 text-orange-700",
                          priority: "bg-amber-400",
                        },
                        {
                          title: "Chat WebSocket",
                          tag: "Backend",
                          tagColor: "bg-blue-100 text-blue-700",
                          priority: "bg-red-400",
                        },
                      ],
                    },
                    {
                      name: "Done",
                      count: 3,
                      dot: "bg-emerald-500",
                      cards: [
                        {
                          title: "Auth & JWT setup",
                          tag: "Backend",
                          tagColor: "bg-blue-100 text-blue-700",
                          priority: "bg-slate-300",
                        },
                        {
                          title: "Database schema",
                          tag: "Backend",
                          tagColor: "bg-blue-100 text-blue-700",
                          priority: "bg-slate-300",
                        },
                        {
                          title: "File upload service",
                          tag: "Infra",
                          tagColor: "bg-pink-100 text-pink-700",
                          priority: "bg-slate-300",
                        },
                      ],
                    },
                  ].map((col) => (
                    <div key={col.name} className="flex flex-col gap-2">
                      <div className="flex items-center gap-1.5 mb-1">
                        <span
                          className={`w-2 h-2 rounded-full ${col.dot}`}
                        />
                        <span className="text-xs font-semibold text-slate-600 uppercase tracking-wide">
                          {col.name}
                        </span>
                        <span className="text-xs text-slate-400 ml-0.5">
                          {col.count}
                        </span>
                      </div>
                      {col.cards.map((card) => (
                        <div
                          key={card.title}
                          className="bg-white border border-slate-200 rounded-lg p-2.5 shadow-sm hover:shadow-md transition-shadow cursor-pointer"
                        >
                          <span
                            className={`inline-block text-[10px] font-semibold px-1.5 py-0.5 rounded ${card.tagColor} mb-1.5`}
                          >
                            {card.tag}
                          </span>
                          <p className="text-xs text-slate-700 font-medium leading-snug">
                            {card.title}
                          </p>
                        </div>
                      ))}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Glow */}
          <div className="absolute -bottom-8 left-1/2 -translate-x-1/2 w-3/4 h-16 bg-indigo-400/20 blur-3xl rounded-full pointer-events-none" />
        </div>
      </section>

      {/* ───────── Features ───────── */}
      <section className="py-24 px-6 bg-slate-50">
        <div className="max-w-5xl mx-auto">
          <div className="text-center mb-14">
            <h2 className="text-4xl font-extrabold tracking-tight text-slate-900">
              Everything your team needs
            </h2>
            <p className="mt-4 text-lg text-slate-500 max-w-2xl mx-auto text-center">
              A unified toolkit for planning, collaboration, and communication —
              so your team stays in flow.
            </p>
          </div>

          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              {
                icon: <LayoutDashboard className="w-5 h-5" />,
                title: "Kanban Boards",
                desc: "Drag-and-drop boards to organize tasks by status, priority, and assignee. Visualize your entire workflow.",
                iconBg: "bg-indigo-100 text-indigo-600",
              },
              {
                icon: <MessageSquare className="w-5 h-5" />,
                title: "Real-time Chat",
                desc: "Workspace channels and direct messages. Link tasks and docs directly in conversations for full context.",
                iconBg: "bg-violet-100 text-violet-600",
              },
              {
                icon: <FileText className="w-5 h-5" />,
                title: "Document Hub",
                desc: "Create collaborative documents, add inline comments, and keep all project knowledge in one place.",
                iconBg: "bg-emerald-100 text-emerald-600",
              },
              {
                icon: <Users className="w-5 h-5" />,
                title: "Team Management",
                desc: "Fine-grained roles — Owner, Admin, Member, Viewer. Control who can see, edit, and manage.",
                iconBg: "bg-amber-100 text-amber-600",
              },
              {
                icon: <Zap className="w-5 h-5" />,
                title: "Activity Tracking",
                desc: "Full audit trail of every change, assignment, and milestone. Never miss what happened while you were away.",
                iconBg: "bg-rose-100 text-rose-600",
              },
              {
                icon: <Shield className="w-5 h-5" />,
                title: "Secure by Design",
                desc: "JWT authentication with Google & GitHub OAuth2. 256-bit encryption. Enterprise-grade from day one.",
                iconBg: "bg-sky-100 text-sky-600",
              },
            ].map((f) => (
              <div
                key={f.title}
                className="bg-white rounded-2xl p-6 border border-slate-200 hover:border-indigo-200 hover:shadow-lg hover:shadow-indigo-50 transition-all"
              >
                <div
                  className={`w-10 h-10 rounded-xl flex items-center justify-center mb-4 ${f.iconBg}`}
                >
                  {f.icon}
                </div>
                <h3 className="font-bold text-slate-800 mb-2">{f.title}</h3>
                <p className="text-sm text-slate-500 leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ───────── Stats ───────── */}
      <section className="py-16 px-6 bg-indigo-600">
        <div className="max-w-4xl mx-auto grid grid-cols-2 sm:grid-cols-4 gap-8 text-center">
          {[
            { value: "99.9%", label: "Uptime guarantee" },
            { value: "<50ms", label: "API response time" },
            { value: "256-bit", label: "AES encryption" },
            { value: "Real-time", label: "Live collaboration" },
          ].map((s) => (
            <div key={s.label}>
              <p className="text-3xl font-extrabold text-white">{s.value}</p>
              <p className="text-sm text-indigo-200 mt-1">{s.label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ───────── CTA ───────── */}
      <section className="py-24 px-6 bg-white">
        <div className="max-w-2xl mx-auto text-center">
          <h2 className="text-4xl font-extrabold tracking-tight text-slate-900">
            Ready to ship faster?
          </h2>
          <p className="mt-4 text-lg text-slate-500">
            Create your workspace in under a minute. Invite your team, set up
            boards, and start collaborating today.
          </p>
          <Link
            href="/register"
            className="mt-8 inline-flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold px-8 py-4 rounded-xl transition-colors shadow-lg shadow-indigo-200 text-base"
          >
            Create your workspace <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </section>

      {/* ───────── Footer ───────── */}
      <footer className="border-t border-slate-100 py-8 px-6">
        <div className="max-w-5xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4 text-sm text-slate-400">
          <span>© {new Date().getFullYear()} CollabNest</span>
          <div className="flex items-center gap-4">
            <Link href="/privacy" className="hover:text-slate-600 transition-colors">Privacy</Link>
            <Link href="/terms" className="hover:text-slate-600 transition-colors">Terms</Link>
            <Link href="/contact" className="hover:text-slate-600 transition-colors">Contact</Link>
          </div>
        </div>
      </footer>
    </div>
  );
}