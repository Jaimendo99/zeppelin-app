package com.zeppelin.app.screens.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class LoginFormData(
    val isError: Boolean,
)


@Serializable
data class DevBrowserTokenResponse(
    val token: String
)

@Serializable
data class SignInRequest(
    val strategy: String = "password",
    val identifier: String,
    val password: String
)

@Serializable
data class SignInResponse(
    val client: Client
)

@Serializable
data class Client(
    val sessions: List<Session>
)

@Serializable
data class Session(
    @SerialName("last_active_token")
    val lastActiveToken: SessionToken
)

@Serializable
data class SessionToken(
    val jwt: String
)

@Serializable
data class ErrorResponseBody(
    val errors: List<ErrorResponse>
)

@Serializable
data class ErrorResponse(
    val message: String
)



