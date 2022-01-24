package com.jvoyatz.beatit.domain.usecases

import com.google.common.truth.Truth.assertThat
import com.jvoyatz.beatit.common.Resource
import com.jvoyatz.beatit.data.repository.FakePlacesRepository
import com.jvoyatz.beatit.domain.Place
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class SearchPlacesUseCaseTest {

    private lateinit var searchPlaces: SearchPlaces
    private lateinit var placesRepository: FakePlacesRepository

    @Before
    public fun setUp() {
        placesRepository = FakePlacesRepository()
        searchPlaces = SearchPlaces(placesRepository)

        val places = mutableListOf<Place>()
        ('a'..'d').forEachIndexed { index, c ->
            places.add(Place(fsqId = "$c 1", placePhotoUrl = "url $c", name = "$c$c$c name"))
        }
        placesRepository.addPlaces(places)
    }

    @Test
    fun `places resource success`() = runBlocking{
        val resource = searchPlaces("37.02, 22,01").first()
        assertThat(resource).isInstanceOf(Resource.Success::class.java)

        if(resource is Resource.Success){
            assertThat(resource.data).isNotEmpty()
        }
    }

    @Test
    fun `places resource error`() = runBlocking{
        placesRepository.clear()
        val resource = searchPlaces("37.02, 22,01").first()
        assertThat(resource).isInstanceOf(Resource.Error::class.java)

        if(resource is Resource.Error){
            assertThat(resource.data).isNotEmpty()
        }
    }
}