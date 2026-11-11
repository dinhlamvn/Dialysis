package com.dialysis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dialysis.app.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: WeightEntryEntity): Long

    @Query(
        """
        SELECT *
        FROM weight_entries
        ORDER BY day_start_millis DESC
        LIMIT 1
        """
    )
    fun observeLatestEntry(): Flow<WeightEntryEntity?>

    @Query(
        """
        SELECT *
        FROM weight_entries
        WHERE day_start_millis BETWEEN :startMillis AND :endMillis
        ORDER BY day_start_millis ASC
        """
    )
    fun observeEntriesInRange(startMillis: Long, endMillis: Long): Flow<List<WeightEntryEntity>>

    @Query(
        """
        SELECT *
        FROM weight_entries
        ORDER BY day_start_millis DESC
        LIMIT :limit
        """
    )
    fun observeRecentEntries(limit: Int): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries ORDER BY day_start_millis DESC LIMIT 1")
    suspend fun getLatestEntry(): WeightEntryEntity?

    @Query("SELECT * FROM weight_entries WHERE server_id IS NULL ORDER BY day_start_millis ASC")
    suspend fun getPendingEntries(): List<WeightEntryEntity>

    /** No-op when the entry was saved again after it was read, so a newer value stays pending. */
    @Query(
        """
        UPDATE weight_entries
        SET server_id = :serverId
        WHERE id = :id AND updated_at = :updatedAt AND server_id IS NULL
        """
    )
    suspend fun markSynced(id: Long, updatedAt: Long, serverId: Long)

    @Query("DELETE FROM weight_entries WHERE server_id IS NOT NULL")
    suspend fun deleteSyncedEntries()

    /** Replaces synced rows with [entries]; a day holding a pending entry keeps that entry. */
    @Transaction
    suspend fun replaceAllKeepingPending(entries: List<WeightEntryEntity>) {
        deleteSyncedEntries()
        val pendingDays = getPendingEntries().mapTo(HashSet()) { it.dayStartMillis }
        entries.filterNot { it.dayStartMillis in pendingDays }.forEach { upsert(it) }
    }

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM weight_entries")
    suspend fun clearAll()
}
