package com.dialysis.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dialysis.app.data.local.dao.WaterEntryDao
import com.dialysis.app.data.local.entity.WaterEntryEntity

@Database(
    entities = [WaterEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterEntryDao(): WaterEntryDao
}
