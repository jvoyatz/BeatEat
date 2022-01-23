package com.jvoyatz.beateat.common

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * used to wrap elements displayed in the ui
 */
sealed class Resource<T>() {
    class Init<T>: Resource<T>()
    class Loading<T>: Resource<T>()
    data class Success<T>(val data:T, val time: Long = System.currentTimeMillis()): Resource<T>()
    data class Error<T>(val message: String, val data: T? = null): Resource<T>(){

        companion object{
            fun <T>  create(e: Throwable): Error<T> {
                var message: String? = null

                when (e) {
                    is SocketTimeoutException -> message = "connection error"
                    is ConnectException -> message = "no internet access"
                    is UnknownHostException -> message = "no internet access!"
                }

                if(e is HttpException){
                    when(e.code()){
                        in 500 .. 502 -> message = "Internal server error"
                        400 -> message = "bad request"
                        404 -> message = "not found"
                    }
                }
                message = message ?: "unknown error exception"
                return Error(message)
            }
        }
    }
}