package com.zeppelin.app.service.pushNotifications

import android.util.Log
import com.zeppelin.app.screens._common.data.ApiClient
import com.zeppelin.app.screens.auth.data.ErrorResponse
import com.zeppelin.app.screens.auth.domain.NetworkResult
import com.zeppelin.app.screens.auth.domain.safeApiCall
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


suspend fun ApiClient.uploadNewToken(userFcmToken: UserFcmToken): NetworkResult<SuccessMessage, ErrorResponse> {
    Log.d("FcmTokenClient", "uploadNewToken: $userFcmToken")
    return safeApiCall {
        client.post {
            url { path("fcm","token") }
            contentType(ContentType.Application.Json)
            setBody(userFcmToken)
        }
    }
}

suspend fun ApiClient.updateToken(fcmToken: FCMToken): NetworkResult<SuccessMessage, ErrorResponse> {
    return safeApiCall {
        Log.d("FcmTokenClient", "updateToken: $fcmToken")
        client.patch {
            url { path("fcm","token", "mobile") }
            contentType(ContentType.Application.Json)
            setBody(fcmToken)
        }
    }
}

@Serializable
data class UserFcmToken(
    @SerialName("firebase_token")
    val firebaseToken: String,
    @SerialName("device_type")
    val deviceType: String = "MOBILE",
    @SerialName("device_info")
    val deviceInfo: String,
)

@Serializable
data class SuccessMessage(
    @SerialName("Body")
    val body: Message,
){
    @Serializable
    data class Message(
        val message: String,
    )
}

@Serializable
data class FCMToken(
    val firebaseToken: String,
)