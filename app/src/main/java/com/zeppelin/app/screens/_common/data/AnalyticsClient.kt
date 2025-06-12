package com.zeppelin.app.screens._common.data

import android.R.attr.path
import android.util.Log
import com.zeppelin.app.screens.auth.data.AuthPreferences
import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.screens.auth.domain.safeApiCall
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
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AnalyticsClient(
    private val authPreferences: AuthPreferences
) {
    val TAG = "AnalyticsClient"
    internal val client =
        HttpClient(CIO) {
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "anal.focused.uno"
                }
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }

            install(ContentNegotiation){
                json(Json { ignoreUnknownKeys = true })
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


    suspend fun addReport(report: ReportData): NetworkResult<String, String> {
        Log.d(TAG, "addReport: $report")
        if (report.type == null || report.body == null){
            return NetworkResult.Error("Report type or data is null")
        }
        return safeApiCall {
            client.post {
                url { path("add/report") }
                setBody(report)
                contentType(ContentType.Application.Json )
            }
        }
    }
}

