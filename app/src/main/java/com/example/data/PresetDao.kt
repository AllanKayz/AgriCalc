package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.CustomPreset
import com.example.model.PresetType
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {
    @Query("SELECT * FROM custom_presets ORDER BY timestamp DESC")
    fun getAllPresets(): Flow<List<CustomPreset>>

    @Query("SELECT * FROM custom_presets WHERE type = :type ORDER BY timestamp DESC")
    fun getPresetsByType(type: PresetType): Flow<List<CustomPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CustomPreset): Long

    @Update
    suspend fun updatePreset(preset: CustomPreset)

    @Delete
    suspend fun deletePreset(preset: CustomPreset)

    @Query("DELETE FROM custom_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)

    @Query("SELECT COUNT(*) FROM custom_presets")
    suspend fun getPresetCount(): Int
}
