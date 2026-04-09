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

    suspend fun saveDailyWeight(weightKg: Float, dateMillis: Long = System.currentTimeMillis()) {
        val dayStart = startOfDay(dateMillis)
        weightEntryDao.upsert(
            WeightEntryEntity(
                weightKg = weightKg,
                dayStartMillis = dayStart,
                updatedAt = System.currentTimeMillis()
            )
        )
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
