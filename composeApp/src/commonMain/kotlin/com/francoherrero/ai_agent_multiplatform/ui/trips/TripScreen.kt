package com.francoherrero.ai_agent_multiplatform.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.francoherrero.ai_agent_multiplatform.trips.NextTripState
import com.francoherrero.ai_agent_multiplatform.trips.TripsState
import com.francoherrero.ai_agent_multiplatform.ui.trips.components.TripCard
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripScreen(
    onTripClick: (String) -> Unit,
) {
    val viewModel: TripViewModel = koinViewModel()
    val nextTripState by viewModel.nextTripState.collectAsState()
    val allTripsState by viewModel.allTripsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTrips()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Viajes") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Next Trip Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tu Proximo Viaje",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                when (val state = nextTripState) {
                    is NextTripState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is NextTripState.NoTrips -> {
                        NoTripsCard()
                    }
                    is NextTripState.Success -> {
                        TripCard(
                            trip = state.trip,
                            isHighlighted = true,
                            onClick = { onTripClick(state.trip.id) }
                        )
                    }
                    is NextTripState.Error -> {
                        ErrorCard(message = state.message)
                    }
                }
            }

            // All Trips Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Todos tus Viajes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (val state = allTripsState) {
                is TripsState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is TripsState.Success -> {
                    if (state.trips.isEmpty()) {
                        item {
                            NoTripsCard()
                        }
                    } else {
                        items(state.trips) { trip ->
                            TripCard(
                                trip = trip,
                                isHighlighted = false,
                                onClick = { onTripClick(trip.id) }
                            )
                        }
                    }
                }
                is TripsState.Error -> {
                    item {
                        ErrorCard(message = state.message)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun NoTripsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Flight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.height(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tienes viajes programados",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Contacta con nosotros para planificar tu proxima aventura!",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorCard(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error al cargar los viajes",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
