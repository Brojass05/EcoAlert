package com.example.pruebasubicacion.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pruebasubicacion.data.ClimaRepository
import com.example.pruebasubicacion.data.UserPreferences
import com.example.pruebasubicacion.data.dataStore
import com.example.pruebasubicacion.data.model.ClimaEstado

import com.example.pruebasubicacion.util.Log

import kotlinx.coroutines.launch


import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory


class UbicacionViewModel(private val userPreferences: UserPreferences): ViewModel() {
    val lastPmState = userPreferences.lastPmFlow.asLiveData()
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.content.Context
                val userPreferences = UserPreferences((context as android.app.Application).applicationContext.dataStore)
                UbicacionViewModel(userPreferences)
            }
        }
    }

    private val repository = ClimaRepository()

    var state by mutableStateOf(ClimaEstado())
        private set

    fun cargarClima(latitud: Double, longitud: Double) {
        viewModelScope.launch {
            state = state.copy(estaCargando = true, mensajeError = null)
            try {
                val data = repository.getClimaCompleto(latitud, longitud)
                saveLastPm(data.pm25)
                state = state.copy(
                    estaCargando = false,
                    clima = data.clima,
                    nombreUbicacion = data.nombreUbicacion,
                    lastPm = data.pm25
                )

            } catch (e: Exception) {
                Log(mensaje = "Error al cargar datos: " + e.message)
                state = state.copy(
                    estaCargando = false,
                    mensajeError = "Error al obtener datos: ${e.message}"
                )
            }
        }
    }
    fun saveLastPm(value: Float) {
        viewModelScope.launch {
            userPreferences.saveLastPm(value)
        }
    }


}
