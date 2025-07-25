package com.zeppelin.app.screens.auth.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.zeppelin.app.db.PreferencesKeys
import com.zeppelin.app.db.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class AuthPreferences(private val context: Context) : IAuthPreferences {

    override suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
        }
    }

    override  fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN]
        }
    }

    override suspend fun getAuthTokenOnce(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN]
        }.firstOrNull()
    }

    override suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.AUTH_TOKEN)
        }
    }

    override suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    override suspend fun getUserIdOnce(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }.firstOrNull()
    }

    override fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }
    }

    override suspend fun clearUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_ID)
        }
    }

}