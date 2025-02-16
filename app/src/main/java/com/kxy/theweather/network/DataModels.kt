package com.kxy.theweather.network

data class TempDataModel(
    val elevation: Int,
    val generationtime_ms: Double,
    val hourly: Hourly,
    val hourly_units: HourlyUnits,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val timezone_abbreviation: String,
    val utc_offset_seconds: Int
)

data class Hourly(
    val temperature_2m: List<Float>,
    val time: List<String>
)

data class HourlyUnits(
    val temperature_2m: String,
    val time: String
)

/**
 * 天地图返回
 */
data class GeoDataModel(
    val msg: String,
    val result: Result,
    val status: String
)

data class Result(
    val addressComponent: AddressComponent,
    val formatted_address: String,
    val location: Location
)

data class AddressComponent(
    val address: String,
    val address_distance: Int,
    val address_position: String,
    val town:String?,
    val city: String,
    val poi: String,
    val poi_distance: String,
    val poi_position: String,
    val road: String,
    val road_distance: Int
)

data class Location(
    val lat: Double,
    val lon: Double
)