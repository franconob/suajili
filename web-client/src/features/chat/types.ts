import type { components } from "@/src/lib/api.d";

export type Message =
  components["schemas"]["com.francoherrero.ai_agent_multiplatform.model.Message"];
export type Role =
  components["schemas"]["com.francoherrero.ai_agent_multiplatform.model.Role"];

export interface ChatHistory {
  id: string;
  title: string;
  preview: string;
  timestamp: Date;
}

// Mock chat history - in a real app this would come from an API
export const mockChatHistory: ChatHistory[] = [
  {
    id: "1",
    title: "React hooks explanation",
    preview: "Can you explain useEffect...",
    timestamp: new Date(Date.now() - 1000 * 60 * 30),
  },
  {
    id: "2",
    title: "Python async patterns",
    preview: "How do I use async/await...",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2),
  },
  {
    id: "3",
    title: "Database optimization",
    preview: "What are the best practices...",
    timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24),
  },
];
