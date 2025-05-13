package com.zeppelin.app.db

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore


// Define the DataStore at the top level
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

// Define your keys
object PreferencesKeys {
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
    val USER_ID = stringPreferencesKey("user_id")
    val FCM_TOKEN = stringPreferencesKey("fcm_token")
}

