package com.example.kotlin_project_1

import androidx.lifecycle.LiveData

class LocationRepository(private val locationDao: LocationDao) {
    val allLocations: LiveData<List<LocationEntity>> = locationDao.getAllLocations()

    suspend fun insert(location: LocationEntity) {
        locationDao.insert(location)
    }

    suspend fun delete(location: LocationEntity) {
        locationDao.delete(location)
    }

    suspend fun deleteAll() {
        locationDao.deleteAll()
    }
}