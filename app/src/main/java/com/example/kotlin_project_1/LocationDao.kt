package com.example.kotlin_project_1

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY id DESC")
    fun getAllLocations(): LiveData<List<LocationEntity>>

    @Insert
    suspend fun insert(location: LocationEntity)

    @Delete
    suspend fun delete(location: LocationEntity)

    @Query("DELETE FROM locations")
    suspend fun deleteAll()
}