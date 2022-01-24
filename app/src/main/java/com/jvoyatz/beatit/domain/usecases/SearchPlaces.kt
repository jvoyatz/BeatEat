package com.jvoyatz.beatit.domain.usecases

import com.jvoyatz.beatit.common.Resource
import com.jvoyatz.beatit.domain.Place
import com.jvoyatz.beatit.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.*

class SearchPlaces(val repository: PlacesRepository) {
    private val TAG = "SearchPlacesInteractor"
    operator fun invoke(latLonStr: String): Flow<Resource<List<Place>>> {
        return flow {
            repository.getNearbyPlaces(latLonStr)
            .catch { e ->
                e.printStackTrace()
                emit(Resource.Error.create(e))
            }
            .collect {
                emit(Resource.Success(it))
            }
        }
    }
}