"use client";

import { PlusIcon, ChatIcon } from "./icons";
import { useConversations } from "./ConversationsContext";

interface ChatSidebarProps {
  chatId?: string;
  isOpen: boolean;
  onNewChat: () => void;
}

export function ChatSidebar({ chatId, isOpen, onNewChat }: ChatSidebarProps) {
  const { conversations } = useConversations();
  return (
    <aside
      className={`${isOpen ? "w-64" : "w-0"
        } bg-[var(--sidebar-bg)] border-r border-[var(--border-color)] flex flex-col transition-all duration-300 overflow-hidden`}
    >
      {/* New Chat Button */}
      <div className="p-3">
        <button onClick={onNewChat} className="w-full flex items-center gap-2 px-3 py-2.5 rounded-lg border border-[var(--border-color)] hover:bg-[var(--message-user-bg)] transition-colors text-sm font-medium">
          <PlusIcon />
          New chat
        </button>
      </div>

      {/* Chat History */}
      <div className="flex-1 overflow-y-auto px-2">
        <div className="text-xs font-medium text-[var(--text-muted)] px-2 py-2">
          Recent
        </div>
        <nav className="space-y-1">
          {conversations.map((chat) => (
            <a
              key={chat.id}
              href={`/conversations/${chat.id}`}
              className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm hover:bg-[var(--message-user-bg)] transition-colors ${chat.id === chatId ? "bg-[var(--message-user-bg)]" : ""
                }`}
            >
              <ChatIcon />
              <span className="truncate flex-1">{chat.title || "Untitled"}</span>
            </a>
          ))}
        </nav>
      </div>

      {/* User section */}
      <div className="p-3 border-t border-[var(--border-color)]">
        <div className="flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-[var(--message-user-bg)] transition-colors cursor-pointer">
          <div className="w-8 h-8 rounded-full bg-[var(--accent)] flex items-center justify-center text-white text-sm font-medium">
            U
          </div>
          <span className="text-sm font-medium">User</span>
        </div>
      </div>
    </aside>
  );
}
