package com.zeppelin.app.service

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.zeppelin.app.db.PreferencesKeys
import com.zeppelin.app.db.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

interface ILiveSessionPref{
    fun getSessionId(): Flow<String?>
    suspend fun saveSessionId(sessionId: String)
    suspend fun clearSessionId()
    suspend fun getSessionIdOnce(): String?
}



class LiveSessionPref(private val context: Context) : ILiveSessionPref {

    override fun getSessionId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SESSION_ID]
        }
    }

    override suspend fun saveSessionId(sessionId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SESSION_ID] = sessionId
        }
    }

    override suspend fun clearSessionId() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SESSION_ID)
        }
    }

    override suspend fun getSessionIdOnce(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SESSION_ID]
        }.firstOrNull()
    }
}

