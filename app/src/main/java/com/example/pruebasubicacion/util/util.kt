package com.example.pruebasubicacion.util

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import kotlin.math.round

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