"use client";

import { useEffect, useEffectEvent, useRef, useState } from "react";
import { ChatSidebar } from "./ChatSidebar";
import { ChatHeader } from "./ChatHeader";
import { ChatMessages } from "./ChatMessages";
import { ChatInput } from "./ChatInput";
import type { Message } from "./types";

interface ChatViewProps {
  chatId: string;
  initialMessages?: Message[];
}

export function ChatView({ chatId, initialMessages = [] }: ChatViewProps) {
  const [messages, setMessages] = useState<Message[]>(initialMessages);
  const [inputValue, setInputValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const [streamingId, setStreamingId] = useState<string | null>(null);
  const streamingIdRef = useRef<string | null>(null);

  useEffect(() => {
    streamingIdRef.current = streamingId;
  }, [streamingId]);

  const onDone = useEffectEvent(() => {
    setStreamingId(null);
    streamingIdRef.current = null;
  });

  const handleSubmit = async () => {
    if (!inputValue.trim() || isLoading) return;

    const nowIso = new Date().toISOString();

    const placeholderUserId = `${Date.now()}-user-placeholder`;

    const optimisticUser: Message = {
      id: placeholderUserId,
      content: inputValue.trim(),
      role: "USER",
      timestamp: new Date().toISOString(),
    };

    const assistantId = `${Date.now()}-assistant`;

    setMessages((prev) => [
      ...prev,
      optimisticUser,
      {
        id: assistantId,
        content: "",
        role: "ASSISTANT",
        timestamp: nowIso,
      },
    ]);

    setStreamingId(assistantId);
    streamingIdRef.current = assistantId;

    setInputValue("");
    setIsLoading(true);

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API}/chat/${chatId}`, {
        method: "POST",
        body: inputValue.trim(),
      });

      const savedUser: Message = await response.json();

      setMessages((prev) =>
        prev.map((m) => (m.id === placeholderUserId ? savedUser : m))
      );

    } catch (error) {
      console.error("Failed to send message:", error);
      // Optional: write error into the streaming bubble
      setMessages((prev) =>
        prev.map((m) =>
          m.id === streamingIdRef.current
            ? { ...m, content: m.content + "\n\n❌ Error enviando el mensaje." }
            : m
        )
      );
      setStreamingId(null);
      streamingIdRef.current = null;
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const eventSource = new EventSource(
      `${process.env.NEXT_PUBLIC_API}/chat/stream?conversationId=${chatId}`
    );

    eventSource.addEventListener("delta", (event) => {
      const chunk = (event as MessageEvent).data ?? "";
      const sid = streamingIdRef.current;
      if (!sid) return;

      const data: { delta: string } = JSON.parse(chunk) ?? { delta: "" };

      setMessages((prev) =>
        prev.map((m) =>
          m.id === sid ? { ...m, content: m.content + data.delta } : m
        )
      );
    });

    eventSource.addEventListener("done", onDone);

    return () => {
      eventSource.close();
    };
  }, [chatId]);

  return (
    <div className="flex h-screen bg-[var(--background)]">
      <ChatSidebar chatId={chatId} isOpen={sidebarOpen} />

      <main className="flex-1 flex flex-col min-w-0">
        <ChatHeader onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} />

        <ChatMessages messages={messages} isLoading={isLoading} />

        <ChatInput
          value={inputValue}
          onChange={setInputValue}
          onSubmit={handleSubmit}
          isLoading={isLoading}
        />
      </main>
    </div>
  );
}
