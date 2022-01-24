package com.jvoyatz.beatit.ui.map

import com.google.common.truth.Truth.assertThat
import com.jvoyatz.beatit.common.Resource
import com.jvoyatz.beatit.data.repository.FakePlacesRepository
import com.jvoyatz.beatit.domain.Place
import com.jvoyatz.beatit.domain.usecases.SearchPlaces
import com.jvoyatz.beatit.domain.usecases.UseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test


class MapViewModelTest() {
    val dispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MapViewModel
    private lateinit var repository: FakePlacesRepository

    @Before
    fun setup(){
        repository = FakePlacesRepository()
        val places = mutableListOf<Place>()
        ('a'..'d').forEachIndexed { index, c ->
            places.add(Place(fsqId = "$c 1", placePhotoUrl = "url $c", name = "$c$c$c name"))
        }
        repository.addPlaces(places)

        val searchPlaces = SearchPlaces(repository)
        viewModel = MapViewModel(UseCases(searchPlaces))
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown(){
        Dispatchers.resetMain()
    }
    @Test
    fun `viewmodel_searchplaces_init`()= runTest{
//        with(dispatcher) {
//            viewModel.searchForPlaces("234234.234")
//            //assertThat(viewModel.placeState.first()).isInstanceOf(Resource.Init::class.java)
//            vie
//        }

        //viewModel.searchForPlaces()

    }
}