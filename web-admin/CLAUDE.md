# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Web admin interface for a travel agency (CRUD operations for catalogs, trips, users, bookings, etc.). Built with **Refine.dev** (self-hosted) on **Next.js 15** using **Ant Design** as the UI framework. Communicates with a **Ktor** backend server located at `/home/fherrero/AndroidStudioProjects/ai-agent-multiplatform/server`.

## Commands

- `pnpm dev` - Start development server (port 3000)
- `pnpm build` - Production build
- `pnpm start` - Run production server
- `pnpm lint` - Run ESLint (next lint)
- `pnpm gen:api` - Regenerate TypeScript types from the server's OpenAPI spec (requires server running at localhost:8080). Output: `src/api/api.ts`

## Architecture

### Framework Stack

- **Refine.dev** handles routing, data fetching, auth, and CRUD scaffolding. All new pages/resources must use Refine's built-in hooks and components (`useList`, `useForm`, `useTable`, `useShow`, etc.).
- **Next.js App Router** - pages live under `src/app/`. Refine is configured in `src/app/layout.tsx` as the root provider.
- **Ant Design** - UI components. Theme is configured via `@refinedev/antd` with `RefineThemes.Blue` and dark/light mode support.

### Key Providers (src/providers/)

- **data-provider**: Currently points to Refine's fake REST API (`https://api.fake-rest.refine.dev`). Needs to be switched to the real Ktor backend using `apiClient`.
- **auth-provider**: Split into client (`auth-provider.client.ts`) and server (`auth-provider.server.ts`) versions. Client handles login/logout/identity via cookies. Server does SSR auth checks via Next.js cookies.
- **devtools**: Wraps Refine DevTools panel.

### API Client (src/api/api-client.ts)

Axios instance configured with `NEXT_PUBLIC_API_URL` as base URL. Attaches JWT from `localStorage` key `access_token` on every request. Auto-removes token and can redirect on 401.

### Path Aliases (tsconfig.json)

- `@*` maps to `./src/*` (e.g., `@providers/data-provider`, `@components/header`, `@contexts/color-mode`)
- `@pages/*` maps to `./pages/*`

### Server Communication

- REST API with OpenAPI spec at `http://localhost:8080/api.json`
- JWT auth: tokens stored in `localStorage` as `access_token` and `refresh_token`

## Adding New CRUD Resources

1. Create page files under `src/app/<resource-name>/` following Next.js App Router conventions
2. Use Refine hooks (`useTable`, `useForm`, `useShow`, `useList`) for data operations
3. Register the resource in the `<Refine>` component's `resources` prop in `src/app/layout.tsx`
4. Use Ant Design components for the UI

## Useful commands
- Use `rg` instead of `grep` it is much faster
- Use `fd` instead of `find` it is much faster