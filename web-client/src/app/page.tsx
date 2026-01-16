import { redirect } from "next/navigation";

export default function Home() {
  // Redirect to a default chat
  redirect("/chat/1");
}
