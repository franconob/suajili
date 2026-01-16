package com.francoherrero.ai_agent_multiplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform