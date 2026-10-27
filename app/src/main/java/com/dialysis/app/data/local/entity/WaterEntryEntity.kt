package com.dialysis.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One drink record. [createdAt] is the logged (drink) time; [createdAtLocal] is when the record was
 * created on this device. [clientId] is generated once at insert and identifies the record in
 * `POST mobile/sync`, so a retried sync never creates a second server row.
 */
@Entity(
    tableName = "water_entries",
    indices = [Index(value = ["client_id"], name = "index_water_entries_client_id", unique = true)]
)
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "client_id")
    val clientId: String,
    @ColumnInfo(name = "drink_name")
    val drinkName: String,
    @ColumnInfo(name = "amount_ml")
    val amountMl: Int,
    @ColumnInfo(name = "weight_ratio")
    val weightRatio: Double,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "created_at_local")
    val createdAtLocal: Long,
    @ColumnInfo(name = "synced_id")
    val syncedId: Long? = null,
)
