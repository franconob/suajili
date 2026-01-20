"use client";

import React from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
// optional:
// import rehypeHighlight from "rehype-highlight";
// import "highlight.js/styles/github.css";

type Props = {
    content: string;
};

export function ChatMarkdown({ content }: Props) {
    return (
        <div className="  prose prose-sm max-w-none
  prose-ul:pl-4
  prose-li:my-1">
            <ReactMarkdown
                remarkPlugins={[remarkGfm]}
            // If you enable highlight:
            // rehypePlugins={[rehypeHighlight]}
            // components={{
            //     a: ({ node, ...props }) => (
            //         <a {...props} target="_blank" rel="noreferrer" />
            //     ),
            //     // Make sure images don’t explode layout
            //     img: ({ node, ...props }) => (
            //         <img {...props} className="max-w-full rounded-lg" />
            //     ),
            // }}
            >
                {content}
            </ReactMarkdown>
        </div>
    );
}
