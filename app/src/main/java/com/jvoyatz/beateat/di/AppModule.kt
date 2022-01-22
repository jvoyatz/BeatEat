package com.jvoyatz.beateat.di

import com.jvoyatz.beateat.common.URL
import com.jvoyatz.beateat.data.PlacesRepositoryImpl
import com.jvoyatz.beateat.data.network.FoursquareApiService
import com.jvoyatz.beateat.domain.repository.PlacesRepository
import com.jvoyatz.beateat.domain.usecases.SearchPlacesInteractor
import com.jvoyatz.beateat.domain.usecases.UseCases
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()


    @Provides
    fun providesInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    @Singleton
    @Provides
    fun provideOkHttp(interceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .retryOnConnectionFailure(true)
            .build();

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()


    @Provides
    fun provideItemService(retrofit: Retrofit): FoursquareApiService = retrofit.create(FoursquareApiService::class.java)


    @Provides
    fun provideIODispatcher() : CoroutineDispatcher = Dispatchers.IO


    @Provides
    @Singleton
    fun provideItemRepository(foursquareApi: FoursquareApiService, dispatcher: CoroutineDispatcher):
            PlacesRepository = PlacesRepositoryImpl(foursquareApi, dispatcher)

    @Provides
    fun provideItemsInteractors(repository: PlacesRepository) =
        UseCases(
            SearchPlacesInteractor(repository),
        )
}