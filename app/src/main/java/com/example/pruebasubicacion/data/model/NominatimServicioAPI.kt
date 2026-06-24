package com.example.pruebasubicacion.data.model

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface NominatimServicioAPI {

    @Headers("User-Agent: AppCalidadAireUCSH/1.0 (y60jaja@gmail.com)") // Usa un correo real
    @GET("reverse")
    suspend fun obtenerDireccion(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json"
    ): NominatimModel

    companion object {
        private var servicioAPI: NominatimServicioAPI? = null
        private const val BASE_URL = "https://nominatim.openstreetmap.org/"

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        fun getInstance(): NominatimServicioAPI {
            if (servicioAPI == null) {
                servicioAPI = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(NominatimServicioAPI::class.java)
            }
            return servicioAPI!!
        }
    }
}
