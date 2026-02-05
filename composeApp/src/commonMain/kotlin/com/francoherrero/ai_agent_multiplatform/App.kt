package com.francoherrero.ai_agent_multiplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.francoherrero.ai_agent_multiplatform.auth.AuthRepository
import com.francoherrero.ai_agent_multiplatform.auth.AuthState
import com.francoherrero.ai_agent_multiplatform.navigation.AppNavigation
import com.francoherrero.ai_agent_multiplatform.ui.theme.SuajiliTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    SuajiliTheme {
        val authRepository = koinInject<AuthRepository>()
        val authState by authRepository.authState.collectAsState()

        var isLoggedIn by remember { mutableStateOf(false) }

        // Check initial auth state
        LaunchedEffect(Unit) {
            authRepository.checkAuthState()
        }

        // Update isLoggedIn based on auth state
        LaunchedEffect(authState) {
            isLoggedIn = authState is AuthState.Authenticated
        }

        AppNavigation(
            isLoggedIn = isLoggedIn,
            onLoginSuccess = {
                isLoggedIn = true
            },
            onLogout = {
                isLoggedIn = false
            }
        )
    }
}
