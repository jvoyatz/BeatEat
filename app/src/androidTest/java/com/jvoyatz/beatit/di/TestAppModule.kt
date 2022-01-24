package com.jvoyatz.beatit.di

import com.jvoyatz.beatit.common.URL
import com.jvoyatz.beatit.data.PlacesRepositoryImpl
import com.jvoyatz.beatit.data.network.AuthInterceptor
import com.jvoyatz.beatit.data.network.FoursquareApiService
import com.jvoyatz.beatit.domain.repository.PlacesRepository
import com.jvoyatz.beatit.domain.usecases.SearchPlaces
import com.jvoyatz.beatit.domain.usecases.UseCases
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TestAppModule {

}