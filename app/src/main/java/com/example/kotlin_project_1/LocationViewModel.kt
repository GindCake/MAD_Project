package com.example.kotlin_project_1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LocationRepository
    val allLocations: LiveData<List<LocationEntity>>

    init {
        val locationDao = AppDatabase.getDatabase(application).locationDao()
        repository = LocationRepository(locationDao)
        allLocations = repository.allLocations
    }

    fun insert(location: LocationEntity) = viewModelScope.launch {
        repository.insert(location)
    }

    fun delete(location: LocationEntity) = viewModelScope.launch {
        repository.delete(location)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}