package com.dialysis.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.dialysis.app.di.CreateDrinkModule
import com.dialysis.app.di.DailyReportModule
import com.dialysis.app.di.HomeModule
import com.dialysis.app.di.InfoModule
import com.dialysis.app.di.LoginModule
import com.dialysis.app.di.NetworkModule
import com.dialysis.app.di.RegisterModule
import com.dialysis.app.di.IntroModule
import com.dialysis.app.di.WeightModule
import com.dialysis.app.di.appModule
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sync.WaterIntakeSyncScheduler
import com.dialysis.app.telemetry.TelemetryIdentity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

class DialysisApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        startKoin {
            androidContext(this@DialysisApplication)
            modules(
                NetworkModule,
                appModule,
                RegisterModule,
                LoginModule,
                CreateDrinkModule,
                HomeModule,
                DailyReportModule,
                InfoModule,
                WeightModule,
                IntroModule
            )
        }
        getKoin().get<TelemetryIdentity>().apply {
            setAppKeys(flavor = BuildConfig.FLAVOR, serverUrl = BuildConfig.SERVER_URL)
            refreshUserId()
        }
        backfillTelemetryUserId()
        getKoin().get<WaterIntakeSyncScheduler>().enqueue()
    }

    /** Sessions logged in before the user id was stored: fetch it once so failure reports name the user. */
    private fun backfillTelemetryUserId() {
        val accountSharePref = getKoin().get<AccountSharePref>()
        if (accountSharePref.getToken().isBlank() || accountSharePref.getUserId() != null) return
        appScope.launch {
            val networkManager = getKoin().get<NetworkManager>()
            val user = networkManager.resolve { networkManager.appServices.me() }.getOrNull() ?: return@launch
            if (accountSharePref.getToken().isBlank()) return@launch
            accountSharePref.setUserId(user.id)
            getKoin().get<TelemetryIdentity>().refreshUserId()
        }
    }
}
