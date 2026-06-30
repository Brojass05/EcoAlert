package com.example.pruebasubicacion.data.model

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface ServicioAPI2 {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("hourly") hourly: String = "pm2_5"
    ): ClimaModel

    companion object {
        private var servicioAPI2: ServicioAPI2? = null
        private const val BASE_URL = "https://air-quality-api.open-meteo.com/"

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        fun getInstance(): ServicioAPI2 {
            if (servicioAPI2 == null) {
                servicioAPI2 = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ServicioAPI2::class.java)
            }
            return servicioAPI2!!
        }
    }
}

