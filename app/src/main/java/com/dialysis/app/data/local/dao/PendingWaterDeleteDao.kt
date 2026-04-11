package com.dialysis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dialysis.app.data.local.entity.PendingWaterDeleteEntity

@Dao
interface PendingWaterDeleteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PendingWaterDeleteEntity): Long

    @Query(
        """
        SELECT *
        FROM pending_water_deletes
        ORDER BY id ASC
        """
    )
    suspend fun getAll(): List<PendingWaterDeleteEntity>

    @Query(
        """
        DELETE FROM pending_water_deletes
        WHERE id = :id
        """
    )
    suspend fun deleteById(id: Long)
}

