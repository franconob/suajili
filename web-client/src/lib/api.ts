import type { paths } from "./api.d";

export type MessageResponse =
  paths["/messages/{conversationId}"]["get"]["responses"][200]["content"]["application/json"];
