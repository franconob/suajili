import { ChatView } from "@/src/features/chat";
import { MessageList } from "@/src/lib/api";

interface ChatPageProps {
  params: Promise<{ id: string }>;
}

export default async function ChatPage({ params }: ChatPageProps) {
  const { id } = await params;
  const initialMessages: MessageList = await fetch(
    `${process.env.NEXT_PUBLIC_API}/conversations/${id}/messages`
  ).then((res) => res.json());

  return <ChatView chatId={id} initialMessages={initialMessages} />;
}
