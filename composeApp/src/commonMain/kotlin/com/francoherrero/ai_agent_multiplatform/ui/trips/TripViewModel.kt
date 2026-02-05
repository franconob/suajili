package com.francoherrero.ai_agent_multiplatform.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francoherrero.ai_agent_multiplatform.trips.NextTripState
import com.francoherrero.ai_agent_multiplatform.trips.TripsState
import com.francoherrero.ai_agent_multiplatform.trips.UserTripsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TripViewModel(
    private val userTripsRepository: UserTripsRepository
) : ViewModel() {

    val nextTripState: StateFlow<NextTripState> = userTripsRepository.nextTripState
    val allTripsState: StateFlow<TripsState> = userTripsRepository.tripsState

    fun loadTrips() {
        viewModelScope.launch {
            userTripsRepository.loadNextTrip()
        }
        viewModelScope.launch {
            userTripsRepository.loadMyTrips()
        }
    }
}
