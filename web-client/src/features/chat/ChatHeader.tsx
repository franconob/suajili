"use client";

import { MenuIcon } from "./icons";

interface ChatHeaderProps {
  onToggleSidebar: () => void;
}

export function ChatHeader({ onToggleSidebar }: ChatHeaderProps) {
  return (
    <header className="h-14 flex items-center px-4 border-b border-[var(--border-color)] bg-[var(--chat-bg)]">
      <button
        onClick={onToggleSidebar}
        className="p-2 hover:bg-[var(--message-user-bg)] rounded-lg transition-colors"
      >
        <MenuIcon />
      </button>
      <h1 className="ml-3 text-lg font-medium">AI Chat</h1>
    </header>
  );
}
