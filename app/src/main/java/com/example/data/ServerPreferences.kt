package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "watchnext_settings")

data class AppSettings(
    val serverUrl: String = "http://192.168.2.11:8000",
    val apiToken: String = "",
    val qbitUrl: String = "http://192.168.2.11:8080",
    val qbitUsername: String = "admin",
    val qbitPassword: String = "",
    val jackettUrl: String = "http://192.168.2.11:9117",
    val jackettApiKey: String = "",
    val jellyfinUrl: String = "http://192.168.2.11:8096",
    val jellyfinApiKey: String = "",
    val episodeLimit: Int = 5,
    val capGb: Float? = null,
    val reserveGb: Float = 5f,
    val isDemoMode: Boolean = false
)

class ServerPreferences(private val context: Context) {

    companion object {
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_API_TOKEN = stringPreferencesKey("api_token")
        val KEY_QBIT_URL = stringPreferencesKey("qbit_url")
        val KEY_QBIT_USER = stringPreferencesKey("qbit_user")
        val KEY_QBIT_PASS = stringPreferencesKey("qbit_pass")
        val KEY_JACKETT_URL = stringPreferencesKey("jackett_url")
        val KEY_JACKETT_KEY = stringPreferencesKey("jackett_key")
        val KEY_JELLYFIN_URL = stringPreferencesKey("jellyfin_url")
        val KEY_JELLYFIN_KEY = stringPreferencesKey("jellyfin_key")
        val KEY_EPISODE_LIMIT = intPreferencesKey("episode_limit")
        val KEY_CAP_GB = floatPreferencesKey("cap_gb")
        val KEY_RESERVE_GB = floatPreferencesKey("reserve_gb")
        val KEY_IS_DEMO = booleanPreferencesKey("is_demo_mode")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val rawCap = prefs[KEY_CAP_GB]
        AppSettings(
            serverUrl = prefs[KEY_SERVER_URL] ?: "http://192.168.2.11:8000",
            apiToken = prefs[KEY_API_TOKEN] ?: "",
            qbitUrl = prefs[KEY_QBIT_URL] ?: "http://192.168.2.11:8080",
            qbitUsername = prefs[KEY_QBIT_USER] ?: "admin",
            qbitPassword = prefs[KEY_QBIT_PASS] ?: "",
            jackettUrl = prefs[KEY_JACKETT_URL] ?: "http://192.168.2.11:9117",
            jackettApiKey = prefs[KEY_JACKETT_KEY] ?: "",
            jellyfinUrl = prefs[KEY_JELLYFIN_URL] ?: "http://192.168.2.11:8096",
            jellyfinApiKey = prefs[KEY_JELLYFIN_KEY] ?: "",
            episodeLimit = prefs[KEY_EPISODE_LIMIT] ?: 5,
            capGb = if (rawCap != null && rawCap > 0f) rawCap else null,
            reserveGb = prefs[KEY_RESERVE_GB] ?: 5f,
            isDemoMode = prefs[KEY_IS_DEMO] ?: false
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = settings.serverUrl
            prefs[KEY_API_TOKEN] = settings.apiToken
            prefs[KEY_QBIT_URL] = settings.qbitUrl
            prefs[KEY_QBIT_USER] = settings.qbitUsername
            prefs[KEY_QBIT_PASS] = settings.qbitPassword
            prefs[KEY_JACKETT_URL] = settings.jackettUrl
            prefs[KEY_JACKETT_KEY] = settings.jackettApiKey
            prefs[KEY_JELLYFIN_URL] = settings.jellyfinUrl
            prefs[KEY_JELLYFIN_KEY] = settings.jellyfinApiKey
            prefs[KEY_EPISODE_LIMIT] = settings.episodeLimit
            if (settings.capGb != null) {
                prefs[KEY_CAP_GB] = settings.capGb
            } else {
                prefs.remove(KEY_CAP_GB)
            }
            prefs[KEY_RESERVE_GB] = settings.reserveGb
            prefs[KEY_IS_DEMO] = settings.isDemoMode
        }
    }

    suspend fun updateLimitsOnly(episodeLimit: Int, capGb: Float?, reserveGb: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EPISODE_LIMIT] = episodeLimit
            if (capGb != null) {
                prefs[KEY_CAP_GB] = capGb
            } else {
                prefs.remove(KEY_CAP_GB)
            }
            prefs[KEY_RESERVE_GB] = reserveGb
        }
    }

    suspend fun setDemoMode(isDemo: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_DEMO] = isDemo
        }
    }
}
