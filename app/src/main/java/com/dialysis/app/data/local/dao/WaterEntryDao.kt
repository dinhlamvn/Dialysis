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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WaterEntryEntity>)

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
        SELECT COALESCE(SUM(amount_ml), 0)
        FROM water_entries
        WHERE created_at BETWEEN :startMillis AND :endMillis
        """
    )
    suspend fun getTotalMl(startMillis: Long, endMillis: Long): Int

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
        SELECT date(created_at / 1000, 'unixepoch', 'localtime') AS day,
               COALESCE(SUM(amount_ml), 0) AS total_ml
        FROM water_entries
        GROUP BY day
        ORDER BY day DESC
        """
    )
    fun observeAllDailyTotals(): Flow<List<DailyTotal>>

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
        SELECT *
        FROM water_entries
        WHERE synced_id IS NULL
        ORDER BY created_at ASC
        """
    )
    suspend fun getUnsyncedEntries(): List<WaterEntryEntity>

    @Query(
        """
        SELECT synced_id
        FROM water_entries
        WHERE synced_id IN (:syncedIds)
        """
    )
    suspend fun getExistingSyncedIds(syncedIds: List<Long>): List<Long>

    @Query(
        """
        SELECT *
        FROM water_entries
        WHERE id = :entryId
        LIMIT 1
        """
    )
    suspend fun getById(entryId: Long): WaterEntryEntity?

    @Query(
        """
        UPDATE water_entries
        SET synced_id = :syncedId
        WHERE id = :entryId
        """
    )
    suspend fun updateSyncedId(entryId: Long, syncedId: Long)

    @Query(
        """
        DELETE FROM water_entries
        WHERE id = :entryId
        """
    )
    suspend fun deleteById(entryId: Long)
}
