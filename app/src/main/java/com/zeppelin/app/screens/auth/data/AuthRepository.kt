package com.zeppelin.app.screens.auth.data

import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.screens.auth.domain.safeApiCall
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType


class AuthRepository(private val networkClient: AuthNetworkClient) : IAuthRepository {
    override suspend fun login(email: String, password: String): NetworkResult<SignInResponse, ErrorResponseBody> {
        return when (val browserTokenResult = networkClient.devBrowser()) {
            is NetworkResult.Success -> {
                val browserToken = browserTokenResult.data.token
                val signInRequest = SignInRequest(identifier = email, password = password)
                networkClient.signIn(signInRequest, browserToken)
            }

            is NetworkResult.Error -> {
                NetworkResult.Error(
                    errorMessage = "Failed to obtain browser token: ${browserTokenResult.errorMessage}",
                    errorCode = browserTokenResult.errorCode,
                    exception = browserTokenResult.exception
                )
            }
            is NetworkResult.Loading -> NetworkResult.Loading
            is NetworkResult.Idle -> NetworkResult.Idle
        }
    }

    override suspend fun logout() {
        TODO()
    }
}

private suspend fun AuthNetworkClient.devBrowser(): NetworkResult<DevBrowserTokenResponse,String > {
    return safeApiCall {
        client.post("dev_browser") {
            contentType(ContentType.Application.FormUrlEncoded)
        }
    }
}

private suspend fun AuthNetworkClient.signIn(
    signInRequest: SignInRequest,
    browserToken: String
): NetworkResult<SignInResponse, ErrorResponseBody> {
    return safeApiCall {
        client.post("client/sign_ins") {
            setBody(FormDataContent(Parameters.build {
                append("strategy", signInRequest.strategy)
                append("identifier", signInRequest.identifier)
                append("password", signInRequest.password)
            }))
            headers {
                append("Authorization", "Bearer $browserToken")
            }
            contentType(ContentType.Application.FormUrlEncoded)
        }
    }
}