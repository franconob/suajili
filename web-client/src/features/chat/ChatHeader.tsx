"use client";

import { MenuIcon } from "./icons";
import { useSidebar } from "./SidebarContext";

export function ChatHeader() {
  const { toggleSidebar } = useSidebar();

  return (
    <header className="h-14 flex items-center px-4 border-b border-[var(--border-color)] bg-[var(--chat-bg)]">
      <button
        onClick={toggleSidebar}
        className="p-2 hover:bg-[var(--message-user-bg)] rounded-lg transition-colors"
      >
        <MenuIcon />
      </button>
      <h1 className="ml-3 text-lg font-medium">AI Chat</h1>
    </header>
  );
}
