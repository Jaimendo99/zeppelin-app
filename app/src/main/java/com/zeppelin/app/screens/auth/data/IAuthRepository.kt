package com.zeppelin.app.screens.auth.data

import com.zeppelin.app.screens.auth.domain.NetworkResult

interface IAuthRepository {
    suspend fun login(email: String, password: String): NetworkResult<SignInResponse, ErrorResponseBody>
    suspend fun logout()

}

