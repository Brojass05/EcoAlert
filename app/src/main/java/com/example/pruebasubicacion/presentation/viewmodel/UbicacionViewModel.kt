package com.example.pruebasubicacion.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pruebasubicacion.data.ClimaRepository
import com.example.pruebasubicacion.data.model.ClimaEstado

import com.example.pruebasubicacion.util.Log

import kotlinx.coroutines.launch


class UbicacionViewModel(): ViewModel() {

    private val repository = ClimaRepository()

    var state by mutableStateOf(ClimaEstado())
        private set

    fun cargarClima(latitud: Double, longitud: Double) {
        viewModelScope.launch {
            state = state.copy(estaCargando = true, mensajeError = null)
            try {
                val data = repository.getClimaCompleto(latitud, longitud)

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


}
