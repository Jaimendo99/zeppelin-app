package com.zeppelin.app.screens.auth.domain

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import java.net.ConnectException


sealed class NetworkResult<out T, out E> {
    data class Success<T>(val data: T) : NetworkResult<T, Nothing>()
    data class Error<E>(
        val errorMessage: String? = null,
        val errorBody: E? = null,
        val errorCode: Int? = null,
        val exception: Throwable? = null
    ) : NetworkResult<Nothing, E>()
    data object Loading : NetworkResult<Nothing, Nothing>()
    data object Idle : NetworkResult<Nothing, Nothing>()
}


suspend inline fun <reified T, reified E> safeApiCall(
    crossinline apiCall: suspend () -> HttpResponse
): NetworkResult<T, E> {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    return try {
        val response = apiCall()

        when (response.status.value) {
            in 200..299 -> {
                NetworkResult.Success(response.body<T>())
            }
            in 400..499 -> {
                val errorBodyString = response.bodyAsText()
                val errorBody: E? = try {
                    json.decodeFromString<E>(errorBodyString)
                } catch (e: Exception) {
                    Log.d("SafeApiCall", "Failed to parse error body: $errorBodyString, ${e.localizedMessage}")
                    null
                }
                NetworkResult.Error(
                    errorBody = errorBody,
                    errorMessage = "Client error",
                    errorCode = response.status.value
                )
            }
            in 500..599 -> {
                NetworkResult.Error(
                    errorMessage = "Server error: ${response.status.description}",
                    errorCode = response.status.value
                )
            }
            else -> {
                NetworkResult.Error(
                    errorMessage = "Unexpected status code: ${response.status.value}",
                    errorCode = response.status.value
                )
            }
        }
    } catch (e: ConnectException) {
        NetworkResult.Error(
            errorMessage = "Network error: Could not connect to the server",
            exception = e
        )
    } catch (e: SocketTimeoutException) {
        NetworkResult.Error(
            errorMessage = "Network timeout: The server took too long to respond",
            exception = e
        )
    } catch (e: HttpRequestTimeoutException) {
        NetworkResult.Error(
            errorMessage = "Request timeout: The server took too long to respond",
            exception = e
        )
    } catch (e: Exception) {
        NetworkResult.Error(
            errorMessage = "Unknown error: ${e.localizedMessage ?: e.toString()}",
            exception = e
        )
    }
}
