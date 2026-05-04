package com.dialysis.app.data.local

import com.dialysis.app.data.local.dao.WeightEntryDao
import com.dialysis.app.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class WeightTrackingRepository(
    private val weightEntryDao: WeightEntryDao
) {

    fun observeLatestEntry(): Flow<WeightEntryEntity?> {
        return weightEntryDao.observeLatestEntry()
    }

    fun observeEntriesInRange(startMillis: Long, endMillis: Long): Flow<List<WeightEntryEntity>> {
        return weightEntryDao.observeEntriesInRange(startOfDay(startMillis), endOfDay(endMillis))
    }

    fun observeRecentEntries(limit: Int = 30): Flow<List<WeightEntryEntity>> {
        return weightEntryDao.observeRecentEntries(limit)
    }

    suspend fun saveDailyWeight(
        weightKg: Float,
        dateMillis: Long = System.currentTimeMillis(),
        serverId: Long? = null,
        note: String = ""
    ) {
        val dayStart = startOfDay(dateMillis)
        weightEntryDao.upsert(
            WeightEntryEntity(
                serverId = serverId,
                weightKg = weightKg,
                dayStartMillis = dayStart,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun replaceAll(entries: List<WeightEntryEntity>) {
        weightEntryDao.clearAll()
        entries.forEach { weightEntryDao.upsert(it) }
    }

    suspend fun delete(entry: WeightEntryEntity) {
        weightEntryDao.deleteById(entry.id)
    }

    private fun startOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun endOfDay(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = startOfDay(timeMillis)
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis
    }
}
