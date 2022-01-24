package com.jvoyatz.beatit.domain.repository

import com.jvoyatz.beatit.domain.Place
import kotlinx.coroutines.flow.Flow

interface PlacesRepository {
    fun getNearbyPlaces(latLonStr: String): Flow<List<Place>>
}