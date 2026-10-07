package com.example.pruebasubicacion.data.model

data class ClimaEstado(
    val estaCargando: Boolean = false,
    val clima: ClimaModel? = null,
    val nombreUbicacion: String? = null,
    val mensajeError: String? = null,
    val lastPm: Float = 0f
)
