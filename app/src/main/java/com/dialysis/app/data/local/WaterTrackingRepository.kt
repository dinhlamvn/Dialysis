package com.dialysis.app.data.local

import com.dialysis.app.data.local.dao.PendingWaterDeleteDao
import com.dialysis.app.data.local.dao.WaterEntryDao
import com.dialysis.app.data.local.entity.PendingWaterDeleteEntity
import com.dialysis.app.data.local.entity.WaterEntryEntity
import com.dialysis.app.data.local.model.DailyTotal
import com.dialysis.app.domain.water.HydrationRatio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Builds a new local record with a fresh, stable `client_id` and the drink's hydration ratio.
 */
fun newWaterEntry(
    drinkName: String,
    amountMl: Int,
    createdAt: Long,
    syncedId: Long? = null,
    weightRatio: Double = HydrationRatio.forDrink(drinkName),
    createdAtLocal: Long = System.currentTimeMillis(),
): WaterEntryEntity = WaterEntryEntity(
    clientId = UUID.randomUUID().toString(),
    drinkName = drinkName,
    amountMl = amountMl,
    weightRatio = weightRatio,
    createdAt = createdAt,
    createdAtLocal = createdAtLocal,
    syncedId = syncedId,
)

class WaterTrackingRepository(
    private val waterEntryDao: WaterEntryDao,
    private val pendingWaterDeleteDao: PendingWaterDeleteDao
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

    fun observeAllEntries(): Flow<List<WaterEntryEntity>> {
        return waterEntryDao.observeAllEntries()
    }

    suspend fun insertEntry(entry: WaterEntryEntity): Long = waterEntryDao.insert(entry)

    suspend fun deleteEntry(entryId: Long) {
        val currentEntry = waterEntryDao.getById(entryId)
        val syncedId = currentEntry?.syncedId
        if (syncedId != null) {
            pendingWaterDeleteDao.insert(PendingWaterDeleteEntity(syncedId = syncedId))
        }
        waterEntryDao.deleteById(entryId)
    }

    suspend fun deleteEntryLocalOnly(entryId: Long) {
        waterEntryDao.deleteById(entryId)
    }

    suspend fun getEntryById(entryId: Long): WaterEntryEntity? {
        return waterEntryDao.getById(entryId)
    }

    suspend fun getUnsyncedEntries(): List<WaterEntryEntity> {
        return waterEntryDao.getUnsyncedEntries()
    }

    suspend fun getUnsyncedEntriesBetween(startMillis: Long, endMillis: Long): List<WaterEntryEntity> {
        return waterEntryDao.getUnsyncedEntriesBetween(startMillis, endMillis)
    }

    suspend fun getSyncedEntries(): List<WaterEntryEntity> {
        return waterEntryDao.getSyncedEntries()
    }

    /**
     * Marks pending records synced (local id -> server id). If another local row already holds that
     * server id (pulled from history before the sync response arrived), that copy is removed so the
     * record is not counted twice.
     */
    suspend fun markEntriesSynced(serverIdByLocalId: Map<Long, Long>) {
        serverIdByLocalId.forEach { (localId, serverId) ->
            val duplicate = waterEntryDao.getBySyncedId(serverId)
            if (duplicate != null && duplicate.id != localId) {
                waterEntryDao.deleteById(duplicate.id)
            }
            waterEntryDao.updateSyncedId(localId, serverId)
        }
    }

    /**
     * Merges server history into local data. Only synced rows are touched: server rows are inserted
     * or updated by server id, and, when [history] is the complete server list, synced rows the server
     * no longer has are removed. Pending rows (`synced_id IS NULL`) are never changed.
     * Server ids in [blockedServerIds] (local deletes not yet pushed) are not re-inserted.
     * Returns the stale synced rows that were removed (empty unless [isCompleteHistory]).
     */
    suspend fun mergeServerHistory(
        history: List<WaterEntryEntity>,
        blockedServerIds: Set<Long>,
        isCompleteHistory: Boolean
    ): List<WaterEntryEntity> {
        val serverEntries = history
            .filter { it.syncedId != null && it.syncedId !in blockedServerIds }
            .distinctBy { it.syncedId }
        val localBySyncedId = waterEntryDao.getSyncedEntries().associateBy { it.syncedId }

        val missing = mutableListOf<WaterEntryEntity>()
        serverEntries.forEach { server ->
            val local = localBySyncedId[server.syncedId]
            when {
                local == null -> missing += server
                local.differsFrom(server) -> waterEntryDao.updateFromServer(
                    entryId = local.id,
                    drinkName = server.drinkName,
                    amountMl = server.amountMl,
                    weightRatio = server.weightRatio,
                    createdAt = server.createdAt
                )
            }
        }
        insertSyncedEntries(missing)

        if (!isCompleteHistory) return emptyList()
        val serverIds = history.mapNotNull { it.syncedId }.toSet()
        val stale = localBySyncedId.values.filter { it.syncedId !in serverIds }
        deleteEntriesLocalOnly(stale.map { it.id })
        return stale
    }

    private fun WaterEntryEntity.differsFrom(other: WaterEntryEntity): Boolean =
        drinkName != other.drinkName ||
            amountMl != other.amountMl ||
            weightRatio != other.weightRatio ||
            createdAt != other.createdAt

    suspend fun insertSyncedEntries(entries: List<WaterEntryEntity>) {
        if (entries.isEmpty()) return
        waterEntryDao.insertAll(entries)
    }

    suspend fun deleteEntriesLocalOnly(entryIds: List<Long>) {
        if (entryIds.isEmpty()) return
        waterEntryDao.deleteByIds(entryIds)
    }

    suspend fun getPendingDeletes(): List<PendingWaterDeleteEntity> {
        return pendingWaterDeleteDao.getAll()
    }

    suspend fun removePendingDelete(id: Long) {
        pendingWaterDeleteDao.deleteById(id)
    }

    suspend fun clearAllLocalData() {
        pendingWaterDeleteDao.clearAll()
        waterEntryDao.clearAll()
    }

    suspend fun getTotalMlForDate(dateMillis: Long): Int {
        return waterEntryDao.getTotalMl(startOfDay(dateMillis), endOfDay(dateMillis))
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
