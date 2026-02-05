package com.francoherrero.ai_agent_multiplatform.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.francoherrero.ai_agent_multiplatform.ui.auth.LoginScreen
import com.francoherrero.ai_agent_multiplatform.ui.auth.RegisterScreen
import com.francoherrero.ai_agent_multiplatform.ui.chat.ChatScreen
import com.francoherrero.ai_agent_multiplatform.ui.contact.ContactScreen
import com.francoherrero.ai_agent_multiplatform.ui.trips.TripDetailsScreen
import com.francoherrero.ai_agent_multiplatform.ui.trips.TripScreen

@Composable
fun AppNavigation(
    isLoggedIn: Boolean,
    onLoginSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    val startDestination: Screen = if (isLoggedIn) Screen.Main else Screen.Login

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    onLoginSuccess()
                    navController.navigate(Screen.Main) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register)
                }
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                onRegisterSuccess = {
                    onLoginSuccess()
                    navController.navigate(Screen.Main) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.Main> {
            MainScreen(
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login) {
                        popUpTo(Screen.Main) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItem.entries.forEach { item ->
                    val selected = currentDestination?.hasRoute(item.route::class) == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.toIcon(),
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Trips,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Trips> {
                TripScreen(
                    onTripClick = { userTripId ->
                        navController.navigate(Screen.TripDetails(userTripId))
                    }
                )
            }

            composable<Screen.TripDetails> { backStackEntry ->
                TripDetailsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Screen.Chat> {
                ChatScreen()
            }

            composable<Screen.Contact> {
                ContactScreen()
            }
        }
    }
}

private fun BottomNavItem.toIcon(): ImageVector = when (this) {
    BottomNavItem.TRIPS -> Icons.Default.Luggage
    BottomNavItem.CHAT -> Icons.AutoMirrored.Filled.Chat
    BottomNavItem.CONTACT -> Icons.Default.Phone
}
