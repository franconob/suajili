package com.francoherrero.ai_agent_multiplatform.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.francoherrero.ai_agent_multiplatform.api.ApiResult
import com.francoherrero.ai_agent_multiplatform.trips.UserTripsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripDetailsViewModel(
    private val userTripsRepository: UserTripsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<TripDetailsState>(TripDetailsState.Loading)
    val state: StateFlow<TripDetailsState> = _state.asStateFlow()

    fun loadTrip(tripId: String) {
        viewModelScope.launch {
            _state.value = TripDetailsState.Loading

            when (val result = userTripsRepository.getTrip(tripId)) {
                is ApiResult.Ok -> {
                    _state.value = TripDetailsState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _state.value = TripDetailsState.Error(result.message)
                }
            }
        }
    }
}
