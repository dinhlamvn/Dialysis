package com.dialysis.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "drink_name")
    val drinkName: String,
    @ColumnInfo(name = "amount_ml")
    val amountMl: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
