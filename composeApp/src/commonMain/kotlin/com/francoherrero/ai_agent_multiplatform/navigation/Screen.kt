package com.francoherrero.ai_agent_multiplatform.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    // Auth screens
    @Serializable
    data object Login : Screen()

    @Serializable
    data object Register : Screen()

    // Main screens (with bottom navigation)
    @Serializable
    data object Main : Screen()

    // Trip screens
    @Serializable
    data object Trips : Screen()

    @Serializable
    data class TripDetails(val userTripId: String) : Screen()

    // Chat screen
    @Serializable
    data object Chat : Screen()

    // Contact screen
    @Serializable
    data object Contact : Screen()
}

enum class BottomNavItem(
    val route: Screen,
    val title: String,
    val iconName: String
) {
    TRIPS(Screen.Trips, "Trips", "luggage"),
    CHAT(Screen.Chat, "Chat", "chat"),
    CONTACT(Screen.Contact, "Contact", "phone")
}
