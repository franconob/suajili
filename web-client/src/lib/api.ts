import type { paths } from "./api.d";

export type MessageList =
  paths["/conversations/{conversationId}/messages"]["get"]["responses"][200]["content"]["application/json"];

  export type Message = MessageList[number]
  export type CreateMessageResponse = paths["/conversations/{conversationId}/messages"]["post"]["responses"][201]["content"]["application/json"];