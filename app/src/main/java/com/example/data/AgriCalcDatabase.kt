package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.CalculationRecord
import com.example.model.CustomPreset

@Database(entities = [CalculationRecord::class, CustomPreset::class], version = 2, exportSchema = false)
abstract class AgriCalcDatabase : RoomDatabase() {
    abstract fun calculationDao(): CalculationDao
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: AgriCalcDatabase? = null

        fun getDatabase(context: Context): AgriCalcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgriCalcDatabase::class.java,
                    "agricalc_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
