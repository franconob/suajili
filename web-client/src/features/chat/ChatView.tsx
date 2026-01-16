"use client";

import { useEffect, useEffectEvent, useState } from "react";
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
  const onDone = useEffectEvent(() => {
    console.log("onDone", deltaList.join(""));
    setMessages((prev) => [
      ...prev,
      {
        id: Date.now().toString(),
        content: deltaList.join(""),
        role: "SYSTEM",
        timestamp: new Date().toISOString(),
      },
    ]);
  });

  const [messages, setMessages] = useState<Message[]>(initialMessages);
  const [deltaList, setDeltaList] = useState<string[]>([]);
  const [inputValue, setInputValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(true);

  const handleSubmit = async () => {
    if (!inputValue.trim() || isLoading) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      content: inputValue.trim(),
      role: "USER",
      timestamp: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputValue("");
    setIsLoading(true);

    try {
      await fetch(`${process.env.NEXT_PUBLIC_API}/chat/${chatId}`, {
        method: "POST",
        body: JSON.stringify(userMessage),
        headers: {
          "Content-Type": "application/json",
        },
      });
    } catch (error) {
      console.error("Failed to send message:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const eventSource = new EventSource(
      `${process.env.NEXT_PUBLIC_API}/chat/stream?conversationId=${chatId}`
    );

    eventSource.addEventListener("delta", (event) => {
      setDeltaList((prev) => [...prev, event.data]);
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
