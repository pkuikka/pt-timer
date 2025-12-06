package com.example.pt_timer

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.pt_timer.data.UserPreferencesRepository

private const val USER_PREFERENCE_NAME = "user_preferences"

class PtTimerApplication : Application() {
    private val dataStore: DataStore<Preferences> by preferencesDataStore(
        name = USER_PREFERENCE_NAME
    )

    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }
}
