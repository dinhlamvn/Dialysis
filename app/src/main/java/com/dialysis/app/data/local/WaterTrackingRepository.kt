package com.dialysis.app.data.local

import com.dialysis.app.data.local.dao.WaterEntryDao
import com.dialysis.app.data.local.entity.WaterEntryEntity
import com.dialysis.app.data.local.model.DailyTotal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WaterTrackingRepository(
    private val waterEntryDao: WaterEntryDao
) {

    fun observeTodayTotalMl(): Flow<Int> {
        val now = System.currentTimeMillis()
        return waterEntryDao.observeTotalMl(startOfDay(now), endOfDay(now))
    }

    fun observeTodayEntries(): Flow<List<WaterEntryEntity>> {
        val now = System.currentTimeMillis()
        return waterEntryDao.observeEntries(startOfDay(now), endOfDay(now))
    }

    fun observeEntriesForDate(dateMillis: Long): Flow<List<WaterEntryEntity>> {
        return waterEntryDao.observeEntries(startOfDay(dateMillis), endOfDay(dateMillis))
    }

    fun observeWeekTotalMl(): Flow<Int> {
        val now = System.currentTimeMillis()
        return waterEntryDao.observeTotalMl(startOfWeek(now), endOfWeek(now))
    }

    fun observeMonthTotalMl(): Flow<Int> {
        val now = System.currentTimeMillis()
        return waterEntryDao.observeTotalMl(startOfMonth(now), endOfMonth(now))
    }

    fun observeTotalMlForDate(dateMillis: Long): Flow<Int> {
        return waterEntryDao.observeTotalMl(startOfDay(dateMillis), endOfDay(dateMillis))
    }

    fun observeWeekDailyMl(): Flow<List<Int>> {
        val now = System.currentTimeMillis()
        val weekStart = startOfWeek(now)
        val weekEnd = endOfWeek(now)
        return waterEntryDao.observeDailyTotals(weekStart, weekEnd)
            .map { totals -> toWeekBuckets(totals, weekStart) }
    }

    fun observeAllDailyTotals(): Flow<List<DailyTotal>> {
        return waterEntryDao.observeAllDailyTotals()
    }

    suspend fun addEntry(drinkName: String, amountMl: Int, createdAt: Long = System.currentTimeMillis()) {
        waterEntryDao.insert(
            WaterEntryEntity(
                drinkName = drinkName,
                amountMl = amountMl,
                createdAt = createdAt
            )
        )
    }

    suspend fun deleteEntry(entryId: Long) {
        waterEntryDao.deleteById(entryId)
    }

    private fun toWeekBuckets(totals: List<DailyTotal>, weekStart: Long): List<Int> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val map = totals.associate { it.day to it.totalMl }
        return (0..6).map { offset ->
            val dayMillis = weekStart + offset * MILLIS_IN_DAY
            val key = formatter.format(Date(dayMillis))
            map[key] ?: 0
        }
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

    private fun startOfWeek(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun endOfWeek(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = startOfWeek(timeMillis)
            add(Calendar.DAY_OF_YEAR, 7)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis
    }

    private fun startOfMonth(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun endOfMonth(timeMillis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = startOfMonth(timeMillis)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis
    }

    private companion object {
        private const val MILLIS_IN_DAY = 24L * 60L * 60L * 1000L
    }
}
