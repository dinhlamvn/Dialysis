package com.dialysis.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}
