package com.zeppelin.app.screens.watchLink.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zeppelin.app.db.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "watch_link_prefs")

class WatchLinkRepository(private val context: Context) {

    companion object {
        private val KEY_WATCH_ADDRESS = stringPreferencesKey("paired_watch_mac_address")
        private val IS_CONNECTED = stringPreferencesKey("is_connected_to_watch")
    }

    val watchAddress: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_WATCH_ADDRESS]
        }

    val isConnectedToWatch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            (preferences[IS_CONNECTED] ?: "false") == "true"
        }

    suspend fun saveIsConnectedToWatch(isConnected: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_CONNECTED] = isConnected.toString()
        }
    }

    suspend fun saveWatchAddress(address: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WATCH_ADDRESS] = address
        }
    }
    suspend fun clearWatchAddress() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_WATCH_ADDRESS)
        }
    }
}