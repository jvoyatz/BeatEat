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
    fun provideOkHttp(interceptor: HttpLoggingInterceptor, authInterceptor: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            //.addInterceptor(interceptor)
            .retryOnConnectionFailure(true)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
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
            SearchPlaces(repository),
        )
}