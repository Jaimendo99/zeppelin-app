package com.zeppelin.app.screens.auth.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.zeppelin.app.db.PreferencesKeys
import com.zeppelin.app.db.dataStore
import io.ktor.util.debug.useContextElementInDebugMode
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
}