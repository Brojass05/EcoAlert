package com.example.pruebasubicacion.data


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class UserPreferences(private val dataStore: DataStore<Preferences>) {
    private val gson = Gson()

    // Definir la llave
    private object PreferencesKeys {
        val LAST_PM_VALUE = floatPreferencesKey("last_pm_value")
        val SENDED_NOTIFICATIONS_JSON = stringPreferencesKey("sended_notifications_json")
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

    // Leer valor de lista (ahora permite duplicados al ser una List)
    val sendedNotificationFlow: Flow<List<String>> = dataStore.data.map { preferences ->
        val json = preferences[PreferencesKeys.SENDED_NOTIFICATIONS_JSON] ?: ""
        if (json.isEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        }
    }

    suspend fun saveLastSetNoti(id: String) {
        dataStore.edit { preferences ->
            val json = preferences[PreferencesKeys.SENDED_NOTIFICATIONS_JSON] ?: ""
            val type = object : TypeToken<MutableList<String>>() {}.type
            val currentList: MutableList<String> = if (json.isEmpty()) {
                mutableListOf()
            } else {
                gson.fromJson(json, type)
            }
            currentList.add(id)
            preferences[PreferencesKeys.SENDED_NOTIFICATIONS_JSON] = gson.toJson(currentList)
        }
    }

    suspend fun resetLastSetNoti() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SENDED_NOTIFICATIONS_JSON] = ""
        }
    }
}
