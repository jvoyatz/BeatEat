package com.jvoyatz.beatit.data.repository

import com.jvoyatz.beatit.domain.Place
import com.jvoyatz.beatit.domain.repository.PlacesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class FakePlacesRepository: PlacesRepository {
    val places = mutableListOf<Place>()

    init {
        places.add(Place(fsqId = "1234", name = "a place", placePhotoUrl = "http://"))
    }

    fun addPlaces(places: List<Place>){
        this.places.addAll(places)
    }

    fun clear(){
        places.clear()
    }
    override fun getNearbyPlaces(latLonStr: String): Flow<List<Place>> {
        return flow {
            if(places.isEmpty())
                throw Exception("an exception")
            emit(places)
        }
    }
}