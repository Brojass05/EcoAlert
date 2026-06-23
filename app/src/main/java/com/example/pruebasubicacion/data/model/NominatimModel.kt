package com.example.pruebasubicacion.data.model

import com.google.gson.annotations.SerializedName

data class NominatimModel(
    @SerializedName("address")
    val address: AddressDetails?
)

data class AddressDetails(
    @SerializedName("suburb") val suburb: String?,
    @SerializedName("city_district") val cityDistrict: String?,
    @SerializedName("town") val town: String?,
    @SerializedName("city") val city: String?, // 👈 Esta es la comuna oficial ("Santiago")
    @SerializedName("state") val state: String?,
    @SerializedName("country") val country: String?
) {
    fun getNombreUbicacion(): String {
        // 🛠️ SOLUCIÓN: Cambiamos el orden. Priorizamos 'city' (Comuna) y 'town' (Pueblo/Ciudad chica)
        // antes de caer en 'suburb' (Barrios que pueden cruzar límites cartográficos).
        return city ?: town ?: suburb ?: cityDistrict ?: "Ubicación desconocida"
    }
}