package com.example.pruebasubicacion.data

import com.example.pruebasubicacion.data.model.ClimaModel
import com.example.pruebasubicacion.data.model.NominatimServicioAPI
import com.example.pruebasubicacion.data.model.ServicioAPI
import com.example.pruebasubicacion.data.model.WeatherServicioAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class ClimaRepository {
    private val servicioAir = ServicioAPI.getInstance()
    private val servicioWeather = WeatherServicioAPI.getInstance()
    private val servicioNominatim = NominatimServicioAPI.getInstance()

    suspend fun getClimaCompleto(lat: Double, lon: Double): ClimaData {
        return withContext(Dispatchers.IO) {
            val airQualityDeferred = async { servicioAir.getAirQuality(lat, lon) }
            val weatherDeferred = async { servicioWeather.getWeatherData(lat, lon) }
            val locationDeferred = async { servicioNominatim.obtenerDireccion(lat, lon) }

            val airData = airQualityDeferred.await()
            val weatherData = weatherDeferred.await()
            val locationData = locationDeferred.await()

            val combinedClima = airData.copy(
                hourly = airData.hourly.copy(
                    humidity = weatherData.hourly.humidity,
                    temperature = weatherData.hourly.temperature
                )
            )

            ClimaData(
                clima = combinedClima,
                nombreUbicacion = locationData.address?.getNombreUbicacion() ?: "Ubicación desconocida",
                pm25 = airData.hourly.pm25?.firstOrNull() ?: 0f
            )
        }
    }

    suspend fun getPm25Only(lat: Double, lon: Double): Float {
        return withContext(Dispatchers.IO) {
            val airData = servicioAir.getAirQuality(lat, lon)
            airData.hourly.pm25?.firstOrNull() ?: 0f
        }
    }
}

data class ClimaData(
    val clima: ClimaModel,
    val nombreUbicacion: String,
    val pm25: Float
)
