package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.CalculationRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {
    @Query("SELECT * FROM calculation_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculation_records WHERE category = :category ORDER BY timestamp DESC")
    fun getRecordsByCategory(category: String): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CalculationRecord): Long

    @Delete
    suspend fun deleteRecord(record: CalculationRecord)

    @Query("DELETE FROM calculation_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM calculation_records")
    suspend fun clearAll()
}
