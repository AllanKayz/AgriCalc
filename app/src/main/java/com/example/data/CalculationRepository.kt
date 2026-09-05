package com.example.data

import com.example.model.CalculationRecord
import kotlinx.coroutines.flow.Flow

class CalculationRepository(private val dao: CalculationDao) {
    val allRecords: Flow<List<CalculationRecord>> = dao.getAllRecords()

    fun getRecordsByCategory(category: String): Flow<List<CalculationRecord>> =
        dao.getRecordsByCategory(category)

    suspend fun insert(record: CalculationRecord): Long = dao.insertRecord(record)

    suspend fun delete(record: CalculationRecord) = dao.deleteRecord(record)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()
}
