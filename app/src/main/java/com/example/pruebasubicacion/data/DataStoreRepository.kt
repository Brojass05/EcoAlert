package com.example.pruebasubicacion.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import android.content.Context
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val dataStore: DataStore<Preferences>) {

    // Definir la llave
    private object PreferencesKeys {
        val LAST_PM_VALUE = floatPreferencesKey("last_pm_value")
    }

    // Leer el valor (devuelve un Flow para ser reactivo)
    val lastPmFlow: Flow<Float> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_PM_VALUE] ?: 0.0f
    }

    // Escribir el valor (función de suspensión)
    suspend fun saveLastPm(value: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PM_VALUE] = value
        }
    }
}