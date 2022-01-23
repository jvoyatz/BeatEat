package com.jvoyatz.beateat.domain.repository

import com.jvoyatz.beateat.common.Resource
import com.jvoyatz.beateat.domain.Place
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun getNearbyPlaces(latLonStr: String): Flow<Resource<List<Place>>>
}