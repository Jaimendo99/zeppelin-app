package com.zeppelin.app.screens.auth.data

import android.util.Log
import com.zeppelin.app.screens._common.data.ApiClient
import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.screens.auth.domain.safeApiCall
import com.zeppelin.app.service.pushNotifications.FcmRepository
import com.zeppelin.app.service.pushNotifications.IFcmRepository
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType


class AuthRepository(
    private val networkClient: AuthNetworkClient,
) : IAuthRepository {
    val Tag = "AuthRepository"

    override suspend fun login(
        email: String,
        password: String
    ): NetworkResult<Session.SessionToken, ErrorResponse> {
        val response = networkClient.signIn(SignInRequest(identifier = email, password = password))
        Log.d(Tag, "login: $response")
        when (response) {
            is NetworkResult.Error -> { Log.d(Tag, "Error: login: ${response.errorBody}")
                return NetworkResult.Error(response.errorBody?.errors?.get(0)?.message)
            }
            NetworkResult.Idle -> return NetworkResult.Idle
            NetworkResult.Loading -> return NetworkResult.Loading
            is NetworkResult.Success -> {
                Log.d(Tag, "Success: login: ${response.data}")
                val session = response.data.client.sessions[0]
                val sessionID = session.id
                val lastToken = session.lastActiveToken.jwt
                return networkClient.getLongerToken(sessionID, lastToken)
            }
        }
    }

    override suspend fun logout() {
        TODO()
    }

}

private suspend fun AuthNetworkClient.getLongerToken(
    sessionID: String,
    lastToken: String,
    jwtTemplate: String = "jwt_template_v1"
): NetworkResult<Session.SessionToken, ErrorResponse> {
    val SERVER_URL = "https://api.focused.uno"
    return safeApiCall {
        client.get("$SERVER_URL/tokenFromSession") {
            parameter("sessionId", sessionID)
            parameter("template", jwtTemplate)
            headers {
                append(HttpHeaders.Authorization, "Bearer $lastToken")
            }
        }
    }
}

private suspend fun AuthNetworkClient.signIn(
    signInRequest: SignInRequest,
): NetworkResult<SignInResponse, ErrorResponseBody> {
    return safeApiCall {
        client.post("client/sign_ins") {
            parameter("_is_native", true)
            setBody(FormDataContent(Parameters.build {
                append("strategy", signInRequest.strategy)
                append("identifier", signInRequest.identifier)
                append("password", signInRequest.password)
            }))
            contentType(ContentType.Application.FormUrlEncoded)
        }
    }
}