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
    @SerializedName("city") val city: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("country") val country: String?
) {
    fun getNombreUbicacion(): String {

        return city ?: town ?: suburb ?: cityDistrict ?: "Ubicación desconocida"
    }
}