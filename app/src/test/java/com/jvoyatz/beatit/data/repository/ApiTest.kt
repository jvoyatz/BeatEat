package com.jvoyatz.beatit.data.repository

import com.jvoyatz.beatit.data.network.FoursquareApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@ExperimentalCoroutinesApi
class ApiTest {
    @get:Rule
    val mockWebServer = MockWebServer()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val fsqService by lazy {
        retrofit.create(FoursquareApiService::class.java)
    }


    @After
    fun tearDown(){
        mockWebServer.shutdown()
    }

    @Test
    fun searchForPlaces() {

    }
}