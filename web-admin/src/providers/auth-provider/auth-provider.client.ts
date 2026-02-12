"use client";

import type { AuthProvider } from "@refinedev/core";
import Cookies from "js-cookie";
import { apiClient } from "@api/api-client";
import type { components } from "@api/api.ts";

type LoginRequestBody =
  components["schemas"]["com.francoherrero.ai_agent_multiplatform.routes.LoginRequestBody"];
type AuthResponse =
  components["schemas"]["com.francoherrero.ai_agent_multiplatform.auth.AuthResponse"];

export const authProviderClient: AuthProvider = {
  login: async ({ email, password }) => {
    try {
      const { data } = await apiClient.post<AuthResponse>("/auth/login", {
        email,
        password,
      } satisfies LoginRequestBody);

      // Decode JWT payload to check role
      const payload = JSON.parse(atob(data.access_token.split(".")[1]!));
      if (payload.user_role !== "admin") {
        return {
          success: false,
          error: { name: "Login Failed", message: "Admin access required" },
        };
      }

      localStorage.setItem("access_token", data.access_token);
      localStorage.setItem("refresh_token", data.refresh_token);

      Cookies.set(
        "auth",
        JSON.stringify({
          id: data.user.id,
          email: data.user.email,
          name: data.user.user_metadata?.full_name ?? data.user.email,
        }),
        { path: "/" },
      );

      return { success: true, redirectTo: "/catalogs" };
    } catch (err: any) {
      const message =
        err?.response?.data?.message ??
        err?.message ??
        "Invalid email or password";

      return {
        success: false,
        error: { name: "Login Failed", message },
      };
    }
  },

  logout: async () => {
    try {
      await apiClient.post("/auth/logout");
    } catch {
      // best-effort — don't block logout on server failure
    }

    localStorage.removeItem("access_token");
    localStorage.removeItem("refresh_token");
    Cookies.remove("auth", { path: "/" });

    return { success: true, redirectTo: "/login" };
  },

  check: async () => {
    const token = localStorage.getItem("access_token");
    if (token) {
      return { authenticated: true };
    }

    return {
      authenticated: false,
      logout: true,
      redirectTo: "/login",
    };
  },

  getPermissions: async () => {
    return null;
  },

  getIdentity: async () => {
    const auth = Cookies.get("auth");
    if (auth) {
      const user = JSON.parse(auth);
      return user;
    }
    return null;
  },

  onError: async (error) => {
    if (error.response?.status === 401) {
      return { logout: true };
    }

    return { error };
  },
};
