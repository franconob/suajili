"use client";

import { useEffect, useEffectEvent, useRef, useState } from "react";
import { ChatHeader } from "./ChatHeader";
import { ChatMessages } from "./ChatMessages";
import { ChatInput } from "./ChatInput";
import type { Message } from "@/src/lib/api";

interface ChatViewProps {
  chatId: string;
  initialMessages?: Message[];
}

export function ChatView({ chatId, initialMessages = [] }: ChatViewProps) {
  const [messages, setMessages] = useState<Message[]>(initialMessages);
  const [inputValue, setInputValue] = useState("");
  const [isLoading, setIsLoading] = useState(false);

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

    const content = inputValue.trim();

    // stable client id to reconcile optimistic -> server
    const clientMessageId =
      typeof crypto !== "undefined" && "randomUUID" in crypto
        ? crypto.randomUUID()
        : `${Date.now()}-${Math.random().toString(16).slice(2)}`;

    const placeholderUserId = `client:${clientMessageId}`;

    const optimisticUser: Message = {
      id: placeholderUserId,
      conversationId: chatId,
      role: "USER",
      content,
      clientMessageId,
      status: "sent",
      createdAt: new Date().toISOString(),
    };

    const assistantId = `assistant:${Date.now()}`;

    setMessages((prev) => [
      ...prev,
      optimisticUser,
      {
        id: assistantId,
        conversationId: chatId,
        role: "ASSISTANT",
        content: "",
        status: "streaming",
        createdAt: new Date().toISOString(),
      },
    ]);

    setStreamingId(assistantId);
    streamingIdRef.current = assistantId;

    setInputValue("");
    setIsLoading(true);

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API}/conversations/${chatId}/messages`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ content, clientMessageId }),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const savedUser: Message = (await response.json());
      console.log({ savedUser })

      // Replace optimistic user message by matching clientMessageId (most reliable)
      setMessages((prev) =>
        prev.map((m) =>
          m.clientMessageId && savedUser.clientMessageId && m.clientMessageId === savedUser.clientMessageId
            ? savedUser
            : m.id === placeholderUserId
              ? savedUser
              : m
        )
      );
    } catch (error) {
      console.error("Failed to send message:", error);

      // Write error into the assistant bubble
      setMessages((prev) =>
        prev.map((m) =>
          m.id === streamingIdRef.current
            ? { ...m, content: m.content + "\n\n❌ Error enviando el mensaje.", status: "failed" }
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
      `${process.env.NEXT_PUBLIC_API}/conversations/${chatId}/stream`
    );

    eventSource.addEventListener("delta", (event) => {
      const sid = streamingIdRef.current;
      if (!sid) return;

      const raw = (event as MessageEvent).data ?? "";
      const data: { delta: string } = JSON.parse(raw) ?? { delta: "" };

      setMessages((prev) =>
        prev.map((m) =>
          m.id === sid ? { ...m, content: m.content + data.delta } : m
        )
      );
    });

    eventSource.addEventListener("done", () => {
      // mark assistant status done (optional)
      const sid = streamingIdRef.current;
      if (sid) {
        setMessages((prev) =>
          prev.map((m) => (m.id === sid ? { ...m, status: "done" } : m))
        );
      }
      onDone();
    });

    return () => {
      eventSource.close();
    };
  }, [chatId]);

  return (
    <main className="flex-1 flex flex-col min-w-0">
      <ChatHeader />

      <ChatMessages messages={messages} isLoading={isLoading} />

      <ChatInput
        value={inputValue}
        onChange={setInputValue}
        onSubmit={handleSubmit}
        isLoading={isLoading}
      />
    </main>
  );
}
