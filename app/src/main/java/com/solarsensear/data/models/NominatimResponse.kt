package com.solarsensear.data.models

import com.google.gson.annotations.SerializedName

data class NominatimResponse(
    @SerializedName("display_name")
    val displayName: String = "",
    val address: NominatimAddress? = null
)

data class NominatimAddress(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val county: String? = null,
    val state: String? = null,
    @SerializedName("state_district")
    val stateDistrict: String? = null,
    val country: String? = null,
    val postcode: String? = null
) {
    /** Returns the most specific locality name available. */
    val locality: String
        get() = city ?: town ?: village ?: stateDistrict ?: county ?: "Unknown"
}
