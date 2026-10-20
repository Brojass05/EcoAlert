package com.example.pruebasubicacion.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.pruebasubicacion.data.model.ClimaEstado
import com.example.pruebasubicacion.data.model.ClimaModel
import com.example.pruebasubicacion.data.model.HourlyData
import com.google.android.gms.location.FusedLocationProviderClient
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// Función de utilidad que recibe el contexto y el cliente
@SuppressLint("MissingPermission")
fun getLastKnownLocation(context: Context, fusedLocationClient: FusedLocationProviderClient) {
    try {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val msg = "Lat: ${lat}, Lon: ${lon}"
                    Log.d("LOCATION", msg)
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Ubicación nula. ¿GPS activo?", Toast.LENGTH_SHORT).show()
                }
            }
    } catch (e: SecurityException) {
        Log.e("LOCATION", "Error de seguridad: ${e.message}")
    }
}
fun Log(tag: String = "ProyectoA", mensaje: String) {
    Log.d(tag, mensaje)
}

fun getTime(){
    val actual = LocalDateTime.now()
    Log.d("Hora", actual.toString())
}

object ChequeadorBackground{
    var segundoPlano: Boolean = false
}

/**
 * Suma horas a una lista de strings en formato ISO (con o sin zona horaria).
 * Si el formato es "yyyy-MM-dd'T'HH:mm", usa LocalDateTime.
 */
fun sumarHoras(listaHoras: List<String>, horasASumar: Int): List<String> {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    val horasASumarN = horasASumar.toLong()

    return listaHoras.map { horaStr ->
        try {
            // Intentamos parsear con el formato específico (sin zona)
            LocalDateTime.parse(horaStr.trim(), formatter)
                .plusHours(horasASumarN)
                .format(formatter)
        } catch (e: Exception) {
            // Si falla, intentamos con el formato ISO estándar por si trae segundos o zona
            try {
                LocalDateTime.parse(horaStr.trim())
                    .plusHours(horasASumarN)
                    .format(formatter)
            } catch (e2: Exception) {
                horaStr // Devolvemos el original si  falla
            }
        }
    }
}

// Mantengo esta por compatibilidad si se usa en otros sitios, pero la corrijo
fun sumarHorasUtc(listaHoras: List<String>, horasASumar: Int): List<String> {
    return sumarHoras(listaHoras, horasASumar)
}

// TODO: Generación de Informes y Recomendaciones Preventivas
/*
Criticas:
Particulado Fino (PM2.5)
Ozono (O3)
Particulado Respirable (PM10)
Indice UV (UV Index)
Segun diagnostico alergico o entorno especifico
Polenes
Alder
Birch
Grass
Mugwort
Olive
Ragweed
Monoxido de carbono (CO), Dioxido de nitrogeno (NO2)
tipicos en zonas urbanas y de alto trafico vehicular, usar en caso de estar en ciudad
Baja relevancia
Dioxido de carbono (CO2)
Aerosol Optical Depth (AOD)
Metano (CH4), Amoniaco (NH3)
* */
fun creaInforme(datosPorHora: HourlyData){
    // TODO: Crear informe
    Log("Boton Informe", "Creando informe")


}

