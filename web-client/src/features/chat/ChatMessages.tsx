"use client";

import { useRef, useEffect } from "react";
import { SparkleIcon } from "./icons";
import type { Message } from "./types";
import { ChatMarkdown } from "./ChatMarkdown";

interface ChatMessagesProps {
  messages: Message[];
  isLoading: boolean;
}

export function ChatMessages({ messages, isLoading }: ChatMessagesProps) {
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  if (messages.length === 0) {
    return (
      <div className="flex-1 overflow-y-auto">
        <div className="h-full flex flex-col items-center justify-center px-4">
          <div className="text-[var(--accent)] mb-4 animate-pulse">
            <SparkleIcon />
          </div>
          <h2 className="text-2xl font-semibold mb-2">
            How can I help you today?
          </h2>
          <p className="text-[var(--text-muted)] text-center max-w-md">
            Start a conversation by typing a message below. I&apos;m here to help
            with coding, writing, analysis, and more.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 overflow-y-auto">
      <div className="max-w-3xl mx-auto px-4 py-6">
        {messages.map((message, index) => (
          <div
            key={message.id}
            className="animate-fade-in-up mb-6"
            style={{ animationDelay: `${index * 0.05}s` }}
          >
            <div
              className={`flex gap-4 ${message.role === "USER" ? "justify-end" : "justify-start"
                }`}
            >
              {message.role === "SYSTEM" && (
                <div className="w-8 h-8 rounded-full bg-[var(--accent)] flex items-center justify-center flex-shrink-0">
                  <SparkleIcon />
                </div>
              )}
              <div
                className={`max-w-[80%] rounded-2xl px-4 py-3 ${message.role === "USER"
                  ? "bg-[var(--accent)] text-white"
                  : "bg-[var(--message-assistant-bg)] border border-[var(--border-color)]"
                  }`}
              >
                <div className="text-sm leading-relaxed whitespace-pre-wrap">
                  <ChatMarkdown content={message.content + (isLoading ? " ▍" : "")} />
                </div>
              </div>
              {message.role === "USER" && (
                <div className="w-8 h-8 rounded-full bg-[var(--message-user-bg)] border border-[var(--border-color)] flex items-center justify-center flex-shrink-0 text-sm font-medium">
                  U
                </div>
              )}
            </div>
          </div>
        ))}

        {/* Loading indicator */}
        {isLoading && (
          <div className="flex gap-4 animate-fade-in-up">
            <div className="w-8 h-8 rounded-full bg-[var(--accent)] flex items-center justify-center flex-shrink-0">
              <SparkleIcon />
            </div>
            <div className="bg-[var(--message-assistant-bg)] border border-[var(--border-color)] rounded-2xl px-4 py-3">
              <div className="flex gap-1">
                <div className="w-2 h-2 bg-[var(--text-muted)] rounded-full typing-dot" />
                <div className="w-2 h-2 bg-[var(--text-muted)] rounded-full typing-dot" />
                <div className="w-2 h-2 bg-[var(--text-muted)] rounded-full typing-dot" />
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>
    </div>
  );
}
