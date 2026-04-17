package com.example.kotlin_project_1

data class LocationRecord(
    val latitude: String,
    val longitude: String,
    val altitude: String,
    val timestamp: String
) {
    companion object {
        fun fromString(data: String): LocationRecord? {
            return try {
                // Format: "Lat: 40.3323, Lon: -3.7653, Alt: 100.0 m, Time: 2023-01-01 12:00:00"
                val parts = data.split(", ")
                val lat = parts[0].substringAfter("Lat: ")
                val lon = parts[1].substringAfter("Lon: ")
                val alt = parts[2].substringAfter("Alt: ")
                val time = parts[3].substringAfter("Time: ")
                LocationRecord(lat, lon, alt, time)
            } catch (e: Exception) {
                null
            }
        }
    }
}