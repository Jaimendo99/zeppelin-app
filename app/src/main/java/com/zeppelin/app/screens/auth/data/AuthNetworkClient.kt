package com.zeppelin.app.screens.auth.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class AuthNetworkClient {
    internal val client = HttpClient(CIO) {
        defaultRequest { url("https://crucial-woodcock-33.clerk.accounts.dev/v1/") }
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(Logging) { logger = Logger.SIMPLE }
    }
}