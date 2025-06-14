package com.zeppelin.app.screens.auth.domain

import android.util.Base64
import android.util.Log
import com.zeppelin.app.screens.auth.data.AuthPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthManager(private val authPreferences: AuthPreferences) {

    suspend fun isTokenValid(): Boolean {
        val token = authPreferences.getAuthTokenOnce()
        return token != null && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size != 3) return true
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val jsonObject = org.json.JSONObject(payload)
            if (jsonObject.has("exp")) {
                val expiration = jsonObject.getLong("exp")
                return System.currentTimeMillis() / 1000 >= expiration
            }
            return false
        } catch (e: Exception) {
            return true
        }
    }

    suspend fun saveToken(token: String) {
        Log.d("AuthManager", "saveToken: $token")
        authPreferences.saveToken(token)
    }

    suspend fun saveUserId(jwt: String) {
        Log.d("AuthManager", "saveUserId: $jwt")
        val parts = jwt.split(".")
        if (parts.size == 3) {
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
            val jsonObject = org.json.JSONObject(payload)
            if (jsonObject.has("sub")) {
                val userId = jsonObject.getString("sub")
                Log.d("AuthManager", "saveUserId: $userId")
                authPreferences.saveUserId(userId)
            }
        }
    }

    suspend fun logout() {
        authPreferences.clearToken()
    }

    // Observe authentication state as a Flow
    fun observeAuthState(): Flow<Boolean> {
        val state = authPreferences.getToken().map { token ->
            val userId = authPreferences.getUserIdOnce()
            Log.d("AuthManager", "observeAuthState: $token, ${!isTokenExpired(token?:"")}")
            token != null && !isTokenExpired(token) && !userId.isNullOrEmpty()
        }
        return state
    }
}
