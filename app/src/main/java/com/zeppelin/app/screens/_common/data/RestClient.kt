package com.zeppelin.app.screens._common.data

import android.util.Log
import com.zeppelin.app.screens.auth.data.AuthPreferences
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class RestClient(
    private val authPreferences: AuthPreferences
) {
    companion object{
        private const val TAG = "RestClient"
    }
    internal val client =
        HttpClient(CIO) {
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "rest.focused.uno"
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val token = authPreferences.getAuthTokenOnce()
                        Log.d(TAG, "loadTokens: $token")
                        if (!token.isNullOrEmpty()) BearerTokens(token, token)
                        else null
                    }
                }
            }
        }

    @Serializable
    data class ErrorResponse(
        val code : String,
        val details: String? = null,
        val hint : String? = null,
        val message: String? = null
    )
}

