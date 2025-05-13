package com.zeppelin.app.service.pushNotifications

import com.zeppelin.app.screens._common.data.ApiClient
import com.zeppelin.app.screens.auth.data.ErrorResponse
import com.zeppelin.app.screens.auth.domain.NetworkResult

class FcmRepository(
    private val pushNotiPreferences: PushNotiPreferences,
    private val apiClient: ApiClient
) : IFcmRepository {


    override suspend fun saveToken(userFcmToken: UserFcmToken): NetworkResult<SuccessMessage, ErrorResponse> {
        return if (pushNotiPreferences.getTokenOnce().isNullOrEmpty()) {
            pushNotiPreferences.saveToken(userFcmToken.firebaseToken)
            apiClient.uploadNewToken(userFcmToken)
        } else {
            pushNotiPreferences.saveToken(userFcmToken.firebaseToken)
            apiClient.updateToken(FCMToken(userFcmToken.firebaseToken))
        }
    }

    override suspend fun uploadToken(): NetworkResult<SuccessMessage, ErrorResponse> {
        val token = pushNotiPreferences.getTokenOnce()
        return if (token.isNullOrEmpty()) {
            NetworkResult.Error(errorMessage = "Token is null or empty")
        } else {
            val deviceInfo = "${android.os.Build.MODEL} ${android.os.Build.VERSION.SDK_INT}"
            apiClient.uploadNewToken(UserFcmToken(token, deviceInfo = deviceInfo))
        }
    }

    override suspend fun getSavedToken(): String? {
        TODO("Not yet implemented")
    }
}