package com.dialysis.app

import android.app.Application
import com.dialysis.app.di.CreateDrinkModule
import com.dialysis.app.di.DailyReportModule
import com.dialysis.app.di.HomeModule
import com.dialysis.app.di.InfoModule
import com.dialysis.app.di.LoginModule
import com.dialysis.app.di.NetworkModule
import com.dialysis.app.di.RegisterModule
import com.dialysis.app.di.WeightModule
import com.dialysis.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin

class DialysisApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DialysisApplication)
            modules(NetworkModule, appModule, RegisterModule, LoginModule, CreateDrinkModule, HomeModule, DailyReportModule, InfoModule, WeightModule)
        }
        getKoin().get<com.dialysis.app.sync.WaterIntakeSyncScheduler>().enqueue()
    }
}
