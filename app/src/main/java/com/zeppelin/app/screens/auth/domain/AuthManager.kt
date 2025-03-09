package com.zeppelin.app.screens.auth.domain

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
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
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
        authPreferences.saveToken(token)
    }

    suspend fun logout() {
        authPreferences.clearToken()
    }

    // Observe authentication state as a Flow
    fun observeAuthState(): Flow<Boolean> {
        return authPreferences.getToken().map { token ->
            token != null && !isTokenExpired(token)
        }
    }
}
