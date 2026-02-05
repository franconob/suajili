package com.francoherrero.ai_agent_multiplatform

import androidx.compose.ui.window.ComposeUIViewController
import com.francoherrero.ai_agent_multiplatform.di.initKoin
import org.koin.mp.KoinPlatform

fun MainViewController() = ComposeUIViewController {
    App()
}

// Called from Swift before creating the view controller
fun doInitKoin() {
    try {
        KoinPlatform.getKoin()
    } catch (e: Exception) {
        // Koin not initialized yet
        initKoin()
    }
}
