package com.zeppelin.app.service.pushNotifications

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.zeppelin.app.db.PreferencesKeys
import com.zeppelin.app.db.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class PushNotiPreferences(private val context: Context)  {
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FCM_TOKEN] = token
        }
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.FCM_TOKEN]
        }
    }

    suspend fun getTokenOnce(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.FCM_TOKEN]
        }.firstOrNull()
    }

}