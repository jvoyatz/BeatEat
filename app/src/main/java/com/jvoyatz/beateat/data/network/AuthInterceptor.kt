package com.jvoyatz.beateat.data.network

import com.jvoyatz.beateat.common.TOKEN
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Named


@Named("authInterceptor")
class AuthInterceptor @Inject constructor(): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder: Request.Builder = chain.request().newBuilder()

        requestBuilder
            .header("Accept", "application/json")
            .header("Authorization", TOKEN)

        return chain.proceed(requestBuilder.build())
    }
}