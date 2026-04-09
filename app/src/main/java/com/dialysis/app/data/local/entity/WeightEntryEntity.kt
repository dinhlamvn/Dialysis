package com.dialysis.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    indices = [Index(value = ["day_start_millis"], unique = true)]
)
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Float,
    @ColumnInfo(name = "day_start_millis")
    val dayStartMillis: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
