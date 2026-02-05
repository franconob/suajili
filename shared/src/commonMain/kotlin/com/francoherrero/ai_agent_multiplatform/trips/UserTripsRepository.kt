package com.francoherrero.ai_agent_multiplatform.trips

import com.francoherrero.ai_agent_multiplatform.api.ApiResult
import com.francoherrero.ai_agent_multiplatform.model.TripDocumentDto
import com.francoherrero.ai_agent_multiplatform.model.UserTripDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface TripsState {
    data object Loading : TripsState
    data class Success(val trips: List<UserTripDto>) : TripsState
    data class Error(val message: String) : TripsState
}

sealed interface NextTripState {
    data object Loading : NextTripState
    data object NoTrips : NextTripState
    data class Success(val trip: UserTripDto) : NextTripState
    data class Error(val message: String) : NextTripState
}

class UserTripsRepository(
    private val userTripsApi: UserTripsApi
) {
    private val _tripsState = MutableStateFlow<TripsState>(TripsState.Loading)
    val tripsState: StateFlow<TripsState> = _tripsState.asStateFlow()

    private val _nextTripState = MutableStateFlow<NextTripState>(NextTripState.Loading)
    val nextTripState: StateFlow<NextTripState> = _nextTripState.asStateFlow()

    suspend fun loadMyTrips() {
        _tripsState.value = TripsState.Loading

        when (val result = userTripsApi.getMyTrips()) {
            is ApiResult.Ok -> {
                _tripsState.value = TripsState.Success(result.data)
            }
            is ApiResult.Error -> {
                _tripsState.value = TripsState.Error(result.message)
            }
        }
    }

    suspend fun loadNextTrip() {
        _nextTripState.value = NextTripState.Loading

        when (val result = userTripsApi.getNextTrip()) {
            is ApiResult.Ok -> {
                val trip = result.data
                if (trip != null) {
                    _nextTripState.value = NextTripState.Success(trip)
                } else {
                    _nextTripState.value = NextTripState.NoTrips
                }
            }
            is ApiResult.Error -> {
                _nextTripState.value = NextTripState.Error(result.message)
            }
        }
    }

    suspend fun getTrip(tripId: String): ApiResult<UserTripDto> {
        return userTripsApi.getTrip(tripId)
    }

    suspend fun getTripDocuments(tripId: String): ApiResult<List<TripDocumentDto>> {
        return userTripsApi.getTripDocuments(tripId)
    }
}
