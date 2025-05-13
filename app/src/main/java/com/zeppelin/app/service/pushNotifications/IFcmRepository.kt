package com.zeppelin.app.service.pushNotifications

import com.zeppelin.app.screens.auth.data.ErrorResponse
import com.zeppelin.app.screens.auth.domain.NetworkResult

interface IFcmRepository {
    suspend fun saveToken(userFcmToken: UserFcmToken) : NetworkResult<SuccessMessage, ErrorResponse>
    suspend fun uploadToken(): NetworkResult<SuccessMessage, ErrorResponse>
    suspend fun getSavedToken(): String?
}
