"use client";

import { usePathname, useRouter } from "next/navigation";
import { ChatSidebar } from "@/src/features/chat/ChatSidebar";
import { SidebarProvider, useSidebar } from "@/src/features/chat/SidebarContext";
import { ConversationsProvider, useConversations } from "@/src/features/chat/ConversationsContext";
import type { components } from "@/src/lib/api.d";

type ConversationDto = components["schemas"]["com.francoherrero.ai_agent_multiplatform.model.ConversationDto"];

interface ConversationsLayoutWrapperProps {
  children: React.ReactNode;
  conversations: ConversationDto[];
}

function ConversationsLayoutContent({ children }: { children: React.ReactNode }) {
  const { sidebarOpen } = useSidebar();
  const { refreshConversations } = useConversations();
  const pathname = usePathname();
  const router = useRouter();

  // Extract chatId from pathname (e.g., "/conversations/123" -> "123")
  const chatId = pathname?.startsWith("/conversations/")
    ? pathname.split("/")[2]
    : undefined;

  const handleNewChat = async () => {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API}/conversations`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        title: "New Conversation",
      }),
    });
    if (!response.ok) {
      console.error(`Failed to create conversation: ${response.status}`);
      return;
    }
    const data = await response.json();
    console.log({ data });
    
    // Refresh the conversations list to include the new one
    await refreshConversations();
    
    router.push(`/conversations/${data.conversation.id}`);
  };

  return (
    <div className="flex h-screen bg-[var(--background)]">
      <ChatSidebar chatId={chatId} isOpen={sidebarOpen} onNewChat={handleNewChat} />

      <div className="flex-1 flex flex-col min-w-0">
        {children}
      </div>
    </div>
  );
}

export function ConversationsLayoutWrapper({ children, conversations }: ConversationsLayoutWrapperProps) {
  return (
    <ConversationsProvider initialConversations={conversations}>
      <SidebarProvider>
        <ConversationsLayoutContent>{children}</ConversationsLayoutContent>
      </SidebarProvider>
    </ConversationsProvider>
  );
}
