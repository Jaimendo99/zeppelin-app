package com.zeppelin.app.service

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.zeppelin.app.db.PreferencesKeys
import com.zeppelin.app.db.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

interface ILiveSessionPref{
    fun getSessionId(): Flow<Int?>
    suspend fun saveSessionId(sessionId: Int)
    suspend fun clearSessionId()
    suspend fun getSessionIdOnce(): Int?

    suspend fun saveCurrentCourseId(courseId: Int)
    suspend fun getCourseIdOnce(): Int?
    suspend fun clearCourseId(courseId: Int)
}



class LiveSessionPref(private val context: Context) : ILiveSessionPref {

    override fun getSessionId(): Flow<Int?> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SESSION_ID]
        }
    }

    override suspend fun saveSessionId(sessionId: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SESSION_ID] = sessionId
        }
    }

    override suspend fun clearSessionId() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SESSION_ID)
        }
    }

    override suspend fun getSessionIdOnce(): Int? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SESSION_ID]
        }.firstOrNull()
    }

    override suspend fun saveCurrentCourseId(courseId: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COURSE_ID] = courseId
        }
    }

    override suspend fun getCourseIdOnce(): Int? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.COURSE_ID]
        }.firstOrNull()
    }

    override suspend fun clearCourseId(courseId: Int) {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.COURSE_ID)
        }
    }
}

