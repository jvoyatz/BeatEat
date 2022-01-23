package com.jvoyatz.beateat.domain.usecases

import android.util.Log
import com.jvoyatz.beateat.common.Resource
import com.jvoyatz.beateat.domain.Place
import com.jvoyatz.beateat.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow

class SearchPlacesInteractor(val repository: PlacesRepository) {
    private val TAG = "SearchPlacesInteractor"
    operator fun invoke(latLonStr: String): Flow<Resource<List<Place>>> {
        return flow {
            repository.getNearbyPlaces(latLonStr).collect {
                emit(it)
            }
        }
    }
}