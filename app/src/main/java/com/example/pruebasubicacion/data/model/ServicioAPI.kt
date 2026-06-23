package com.example.pruebasubicacion.data.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ServicioAPI {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "pm2_5,pm10,nitrogen_dioxide,ozone,sulphur_dioxide,carbon_monoxide,uv_index"
    ): ClimaModel

    companion object {
        private var servicioAPI: ServicioAPI? = null
        private const val BASE_URL = "https://air-quality-api.open-meteo.com/"

        fun getInstance(): ServicioAPI {
            if (servicioAPI == null) {
                servicioAPI = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ServicioAPI::class.java)
            }
            return servicioAPI!!
        }
    }
}

interface WeatherServicioAPI {
    @GET("v1/forecast")
    suspend fun getWeatherData(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "relative_humidity_2m,temperature_2m"
    ): ClimaModel

    companion object {
        private var weatherServicioAPI: WeatherServicioAPI? = null
        private const val BASE_URL = "https://api.open-meteo.com/"

        fun getInstance(): WeatherServicioAPI {
            if (weatherServicioAPI == null) {
                weatherServicioAPI = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(WeatherServicioAPI::class.java)
            }
            return weatherServicioAPI!!
        }
    }
}
