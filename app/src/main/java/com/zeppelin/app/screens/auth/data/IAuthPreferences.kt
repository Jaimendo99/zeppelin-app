package com.zeppelin.app.screens.auth.data

import kotlinx.coroutines.flow.Flow

interface IAuthPreferences {
    suspend fun saveToken(token: String)
    fun getToken(): Flow<String?>
    suspend fun clearToken()
    suspend fun getAuthTokenOnce(): String?
}