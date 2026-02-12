"use client";

import dataProviderSimpleRest from "@refinedev/simple-rest";
import { apiClient } from "@api/api-client";

const API_URL = process.env.NEXT_PUBLIC_API_URL!;

export const dataProvider = dataProviderSimpleRest(API_URL, apiClient);
