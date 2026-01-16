import type { paths } from "./api.d";

export type MessageResponse =
  paths["/messages"]["get"]["responses"][200]["content"]["application/json"];
