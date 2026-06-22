package com.moltrax.personalnoteapp.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_prefs")

@Singleton
class AppPreferences @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val THEME_MODE          = stringPreferencesKey("theme_mode")
        val GEMINI_API_KEY      = stringPreferencesKey("gemini_api_key")
        val REMINDER_MINUTES    = intPreferencesKey("reminder_minutes")
        val IS_SIGNED_IN        = booleanPreferencesKey("is_signed_in")
        val DRIVE_FILE_ID       = stringPreferencesKey("drive_file_id")
        val DRIVE_ETAG          = stringPreferencesKey("drive_etag")
        val LAST_SYNC_AT        = stringPreferencesKey("last_sync_at")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    suspend fun setThemeMode(mode: String) = context.dataStore.edit { it[Keys.THEME_MODE] = mode }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { it[Keys.GEMINI_API_KEY] ?: "" }
    suspend fun setGeminiApiKey(key: String) = context.dataStore.edit { it[Keys.GEMINI_API_KEY] = key }

    val reminderMinutes: Flow<Int> = context.dataStore.data.map { it[Keys.REMINDER_MINUTES] ?: 60 }
    suspend fun setReminderMinutes(m: Int) = context.dataStore.edit { it[Keys.REMINDER_MINUTES] = m }

    val isSignedIn: Flow<Boolean> = context.dataStore.data.map { it[Keys.IS_SIGNED_IN] ?: false }
    suspend fun setSignedIn(v: Boolean) = context.dataStore.edit { it[Keys.IS_SIGNED_IN] = v }

    val driveFileId: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVE_FILE_ID] }
    suspend fun setDriveFileId(id: String?) = context.dataStore.edit {
        if (id == null) it.remove(Keys.DRIVE_FILE_ID) else it[Keys.DRIVE_FILE_ID] = id
    }

    val driveEtag: Flow<String?> = context.dataStore.data.map { it[Keys.DRIVE_ETAG] }
    suspend fun setDriveEtag(etag: String?) = context.dataStore.edit {
        if (etag == null) it.remove(Keys.DRIVE_ETAG) else it[Keys.DRIVE_ETAG] = etag
    }

    val lastSyncAt: Flow<String?> = context.dataStore.data.map { it[Keys.LAST_SYNC_AT] }
    suspend fun setLastSyncAt(iso: String?) = context.dataStore.edit {
        if (iso == null) it.remove(Keys.LAST_SYNC_AT) else it[Keys.LAST_SYNC_AT] = iso
    }
}
