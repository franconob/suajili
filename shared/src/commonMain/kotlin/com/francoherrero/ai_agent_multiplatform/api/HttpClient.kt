package com.francoherrero.ai_agent_multiplatform.api

import com.francoherrero.ai_agent_multiplatform.auth.TokenStorage

expect fun createHttpClient(baseUrl: String, tokenStorage: TokenStorage): io.ktor.client.HttpClient