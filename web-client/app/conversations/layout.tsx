import { ConversationsLayoutWrapper } from "./ConversationsLayoutWrapper";
import type { components } from "@/src/lib/api.d";

type ConversationDto = components["schemas"]["com.francoherrero.ai_agent_multiplatform.model.ConversationDto"];

async function getConversations(): Promise<ConversationDto[]> {
  try {
    const response = await fetch(`${process.env.NEXT_PUBLIC_API}/conversations`, {
      cache: "no-store",
    });

    if (!response.ok) {
      console.error(`Failed to fetch conversations: ${response.status}`);
      return [];
    }

    return await response.json();
  } catch (error) {
    console.error("Error fetching conversations:", error);
    return [];
  }
}

export default async function ConversationsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const conversations = await getConversations();

  return (
    <ConversationsLayoutWrapper conversations={conversations}>
      {children}
    </ConversationsLayoutWrapper>
  );
}
