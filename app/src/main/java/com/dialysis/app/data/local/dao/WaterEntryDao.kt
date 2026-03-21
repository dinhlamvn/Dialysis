package com.dialysis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dialysis.app.data.local.entity.WaterEntryEntity
import com.dialysis.app.data.local.model.DailyTotal
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaterEntryEntity): Long

    @Query(
        """
        SELECT COALESCE(SUM(amount_ml), 0)
        FROM water_entries
        WHERE created_at BETWEEN :startMillis AND :endMillis
        """
    )
    fun observeTotalMl(startMillis: Long, endMillis: Long): Flow<Int>

    @Query(
        """
        SELECT date(created_at / 1000, 'unixepoch', 'localtime') AS day,
               COALESCE(SUM(amount_ml), 0) AS total_ml
        FROM water_entries
        WHERE created_at BETWEEN :startMillis AND :endMillis
        GROUP BY day
        ORDER BY day
        """
    )
    fun observeDailyTotals(startMillis: Long, endMillis: Long): Flow<List<DailyTotal>>

    @Query(
        """
        SELECT *
        FROM water_entries
        WHERE created_at BETWEEN :startMillis AND :endMillis
        ORDER BY created_at DESC
        """
    )
    fun observeEntries(startMillis: Long, endMillis: Long): Flow<List<WaterEntryEntity>>

    @Query(
        """
        DELETE FROM water_entries
        WHERE id = :entryId
        """
    )
    suspend fun deleteById(entryId: Long)
}
