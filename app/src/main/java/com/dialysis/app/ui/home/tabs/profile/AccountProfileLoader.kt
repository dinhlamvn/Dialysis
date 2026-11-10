package com.dialysis.app.ui.home.tabs.profile

import com.dialysis.app.data.network.isNoInternetFailure
import com.dialysis.app.data.network.response.ApiResponse
import com.dialysis.app.data.network.response.LoginUser
import retrofit2.HttpException

sealed interface ProfileLoadResult {
    data class Success(val user: LoginUser) : ProfileLoadResult
    data object NoInternet : ProfileLoadResult
    /** 401 that the silent re-login could not fix; the session-expired event was raised. */
    data object SessionExpired : ProfileLoadResult
    data object Failed : ProfileLoadResult
}

fun interface AccountProfileLoader {
    suspend fun load(): ProfileLoadResult
}

/**
 * Loads `GET mobile/auth/me`. In production [fetchMe] goes through the re-login client
 * (`NetworkManager.syncAppServices`: silent re-login + one retry on 401) and [resolve] is
 * `NetworkManager.resolve`. A 401 that still reaches us is final: [onSessionExpired] raises the
 * app-wide event (idempotent). [resolve] drops the HTTP code, so the 401 is noted before it.
 */
class RemoteAccountProfileLoader(
    private val fetchMe: suspend () -> ApiResponse<LoginUser>,
    private val resolve: suspend (suspend () -> ApiResponse<LoginUser>) -> Result<LoginUser>,
    private val onSessionExpired: () -> Unit,
) : AccountProfileLoader {

    override suspend fun load(): ProfileLoadResult {
        var unauthorized = false
        val result = resolve {
            try {
                fetchMe()
            } catch (e: HttpException) {
                if (e.code() == HTTP_UNAUTHORIZED) unauthorized = true
                throw e
            }
        }
        return when {
            result.isSuccess -> ProfileLoadResult.Success(result.getOrThrow())
            unauthorized -> {
                onSessionExpired()
                ProfileLoadResult.SessionExpired
            }
            result.isNoInternetFailure() -> ProfileLoadResult.NoInternet
            else -> ProfileLoadResult.Failed
        }
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
