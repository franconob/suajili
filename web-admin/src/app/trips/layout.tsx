"use client";

import { Header } from "@components/header";
import { ThemedLayout } from "@refinedev/antd";
import React from "react";

export default function CatalogsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <ThemedLayout Header={Header}>{children}</ThemedLayout>;
}
