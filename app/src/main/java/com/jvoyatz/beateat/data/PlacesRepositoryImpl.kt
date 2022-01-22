package com.jvoyatz.beateat.data

import com.jvoyatz.beateat.data.network.FoursquareApiService
import com.jvoyatz.beateat.domain.repository.PlacesRepository
import kotlinx.coroutines.CoroutineDispatcher

class PlacesRepositoryImpl(val foursquareApi: FoursquareApiService, val ioDispatcher: CoroutineDispatcher): PlacesRepository {

}