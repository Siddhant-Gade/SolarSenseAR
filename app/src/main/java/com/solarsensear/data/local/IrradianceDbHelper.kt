package com.solarsensear.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object IrradianceDbHelper {

    private var districtData: Map<String, DistrictInfo>? = null

    data class DistrictInfo(
        val state: String,
        val avg_ghi: Double
    )

    private data class IrradianceData(
        val districts: Map<String, DistrictInfo>
    )

    fun loadData(context: Context) {
        if (districtData != null) return
        try {
            val json = context.assets.open("data/irradiance_india.json")
                .bufferedReader()
                .use { it.readText() }
            val data = Gson().fromJson(json, IrradianceData::class.java)
            districtData = data.districts
        } catch (e: Exception) {
            districtData = emptyMap()
        }
    }

    fun getIrradiance(context: Context, city: String): Double {
        loadData(context)
        return districtData?.get(city)?.avg_ghi
            ?: districtData?.get("DEFAULT")?.avg_ghi
            ?: 5.5
    }

    fun getState(context: Context, city: String): String {
        loadData(context)
        return districtData?.get(city)?.state
            ?: "Unknown"
    }

    fun getAvailableCities(context: Context): List<String> {
        loadData(context)
        return districtData?.keys
            ?.filter { it != "DEFAULT" }
            ?.sorted()
            ?: emptyList()
    }
}
