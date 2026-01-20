import { ChatView } from "@/src/features/chat";
import { MessageResponse } from "@/src/lib/api";

interface ChatPageProps {
  params: Promise<{ id: string }>;
}

export default async function ChatPage({ params }: ChatPageProps) {
  const { id } = await params;
  const initialMessages: MessageResponse = await fetch(`${process.env.NEXT_PUBLIC_API}/messages/${id}`).then(res => res.json());

  return <ChatView chatId={id} initialMessages={initialMessages.messages} />;
}
