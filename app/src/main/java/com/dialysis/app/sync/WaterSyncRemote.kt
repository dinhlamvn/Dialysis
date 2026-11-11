package com.dialysis.app.sync

import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.MobileSyncRequest
import com.dialysis.app.data.network.request.WaterIntakeRequest
import com.dialysis.app.data.network.response.MobileSyncResponseData
import com.dialysis.app.data.network.response.WaterIntakeResponse
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

enum class RemoteDeleteResult { DELETED, NOT_FOUND, FAILED }

sealed interface RemoteCreateResult {
    data class Created(val serverId: Long) : RemoteCreateResult
    data object Failed : RemoteCreateResult
}

/** Server calls used by [WaterSyncEngine] and [WaterIntakeRecorder]. */
interface WaterSyncRemote {
    suspend fun createIntake(request: WaterIntakeRequest): RemoteCreateResult
    suspend fun deleteIntake(serverId: Long): RemoteDeleteResult
    suspend fun syncOffline(request: MobileSyncRequest): Result<MobileSyncResponseData>
    suspend fun getHistoryPage(page: Int): Result<List<WaterIntakeResponse>>
}

class NetworkWaterSyncRemote(
    private val networkManager: NetworkManager
) : WaterSyncRemote {

    override suspend fun createIntake(request: WaterIntakeRequest): RemoteCreateResult {
        return try {
            val response = networkManager.appServices.syncWaterIntake(request)
            val serverId = response.data?.id
            if (response.success && serverId != null) {
                RemoteCreateResult.Created(serverId)
            } else {
                RemoteCreateResult.Failed
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Any failure, including 401, falls back to a pending record (user decision 2026-10-27).
            RemoteCreateResult.Failed
        }
    }

    override suspend fun deleteIntake(serverId: Long): RemoteDeleteResult {
        return try {
            val response = networkManager.syncAppServices.deleteWaterIntake(serverId)
            when {
                response.success -> RemoteDeleteResult.DELETED
                isNotFoundMessage(response.message) -> RemoteDeleteResult.NOT_FOUND
                else -> RemoteDeleteResult.FAILED
            }
        } catch (e: HttpException) {
            val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
            if (e.code() == HTTP_NOT_FOUND || isNotFoundMessage(body)) {
                RemoteDeleteResult.NOT_FOUND
            } else {
                RemoteDeleteResult.FAILED
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RemoteDeleteResult.FAILED
        }
    }

    override suspend fun syncOffline(request: MobileSyncRequest): Result<MobileSyncResponseData> =
        networkManager.resolve { networkManager.syncAppServices.syncOffline(request) }

    override suspend fun getHistoryPage(page: Int): Result<List<WaterIntakeResponse>> =
        networkManager.resolveNullable { networkManager.syncAppServices.getWaterHistory(page = page) }
            .map { it.orEmpty() }

    private companion object {
        const val HTTP_NOT_FOUND = 404

        /** Message the server returns when the intake was already deleted (as matched on iOS). */
        const val NOT_FOUND_MESSAGE = "Record not found"

        fun isNotFoundMessage(message: String?): Boolean =
            message?.contains(NOT_FOUND_MESSAGE, ignoreCase = true) == true
    }
}
