"use client";

import { createContext, useContext, useState, ReactNode, useCallback } from "react";
import type { components } from "@/src/lib/api.d";

type ConversationDto = components["schemas"]["com.francoherrero.ai_agent_multiplatform.model.ConversationDto"];

interface ConversationsContextType {
  conversations: ConversationDto[];
  setConversations: (conversations: ConversationDto[]) => void;
  refreshConversations: () => Promise<void>;
  isLoading: boolean;
}

const ConversationsContext = createContext<ConversationsContextType | undefined>(undefined);

export function ConversationsProvider({
  children,
  initialConversations,
}: {
  children: ReactNode;
  initialConversations: ConversationDto[];
}) {
  const [conversations, setConversations] = useState<ConversationDto[]>(initialConversations);
  const [isLoading, setIsLoading] = useState(false);

  const refreshConversations = useCallback(async () => {
    setIsLoading(true);
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API}/conversations`, {
        cache: "no-store",
      });

      if (!response.ok) {
        console.error(`Failed to fetch conversations: ${response.status}`);
        return;
      }

      const data = await response.json();
      setConversations(data);
    } catch (error) {
      console.error("Error fetching conversations:", error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  return (
    <ConversationsContext.Provider
      value={{ conversations, setConversations, refreshConversations, isLoading }}
    >
      {children}
    </ConversationsContext.Provider>
  );
}

export function useConversations() {
  const context = useContext(ConversationsContext);
  if (context === undefined) {
    throw new Error("useConversations must be used within a ConversationsProvider");
  }
  return context;
}
