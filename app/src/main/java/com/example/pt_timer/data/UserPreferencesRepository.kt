package com.example.pt_timer.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    //  'dataStore' is now a fully initialized property of the class and can be safely accessed.
    val timerWriteDelayMillis: Flow<Float> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            // If the key doesn't exist, return the default value of 100F
            preferences[TIMER_WRITE_DELAY_MILLIS] ?: 100F
        }

    val selectedBtDevice: Flow<String> = dataStore.data
        .catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            // If the key doesn't exist, return an empty string
            preferences[SELECTED_BT_DEVICE] ?: ""
        }

    private companion object {
        val TIMER_WRITE_DELAY_MILLIS = floatPreferencesKey("timer_write_delay_millis")
        val SELECTED_BT_DEVICE = stringPreferencesKey("selected_bt_device")

        const val TAG = "UserPreferencesRepo"
    }

    suspend fun saveLayoutPreference(timerWriteDelayMillis: Float) {
        dataStore.edit { preferences ->
            preferences[TIMER_WRITE_DELAY_MILLIS] = timerWriteDelayMillis
        }
    }

    suspend fun saveSelectedDevice(deviceName: String) {
        dataStore.edit { preferences ->
            preferences[SELECTED_BT_DEVICE] = deviceName
        }
    }
}
