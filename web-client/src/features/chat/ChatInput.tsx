"use client";

import { useRef, useEffect } from "react";
import { SendIcon } from "./icons";

interface ChatInputProps {
  value: string;
  onChange: (value: string) => void;
  onSubmit: () => void;
  isLoading: boolean;
}

export function ChatInput({ value, onChange, onSubmit, isLoading }: ChatInputProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Auto-resize textarea
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = "auto";
      textareaRef.current.style.height = `${Math.min(
        textareaRef.current.scrollHeight,
        200
      )}px`;
    }
  }, [value]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!value.trim() || isLoading) return;
    onSubmit();
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <div className="border-t border-[var(--border-color)] bg-[var(--chat-bg)] p-4">
      <form onSubmit={handleSubmit} className="max-w-3xl mx-auto relative">
        <div className="relative flex items-end bg-[var(--input-bg)] border border-[var(--border-color)] rounded-2xl shadow-sm focus-within:border-[var(--accent)] focus-within:ring-1 focus-within:ring-[var(--accent)] transition-all">
          <textarea
            ref={textareaRef}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Message AI..."
            rows={1}
            className="flex-1 bg-transparent px-4 py-3 pr-12 text-sm resize-none focus:outline-none max-h-[200px] placeholder:text-[var(--text-muted)]"
          />
          <button
            type="submit"
            disabled={!value.trim() || isLoading}
            className="absolute right-2 bottom-2 p-2 rounded-lg bg-[var(--accent)] text-white disabled:opacity-40 disabled:cursor-not-allowed hover:bg-[var(--accent-hover)] transition-colors"
          >
            <SendIcon />
          </button>
        </div>
        <p className="text-xs text-center text-[var(--text-muted)] mt-2">
          AI can make mistakes. Consider checking important information.
        </p>
      </form>
    </div>
  );
}
