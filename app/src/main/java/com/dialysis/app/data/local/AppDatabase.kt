package com.dialysis.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dialysis.app.data.local.dao.PendingWaterDeleteDao
import com.dialysis.app.data.local.dao.WeightEntryDao
import com.dialysis.app.data.local.dao.WaterEntryDao
import com.dialysis.app.data.local.entity.PendingWaterDeleteEntity
import com.dialysis.app.data.local.entity.WeightEntryEntity
import com.dialysis.app.data.local.entity.WaterEntryEntity

@Database(
    entities = [WaterEntryEntity::class, WeightEntryEntity::class, PendingWaterDeleteEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun pendingWaterDeleteDao(): PendingWaterDeleteDao
}
