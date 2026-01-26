"use client";

import { ChatHeader } from "@/src/features/chat/ChatHeader";

export function ConversationsLayout() {
  return (
    <main className="flex-1 flex flex-col min-w-0">
      <ChatHeader />

      {/* Empty middle area for now */}
      <div className="flex-1 flex items-center justify-center">
        <div className="text-center text-[var(--text-muted)]">
          <p className="text-lg">Select a conversation to start chatting</p>
        </div>
      </div>
    </main>
  );
}
