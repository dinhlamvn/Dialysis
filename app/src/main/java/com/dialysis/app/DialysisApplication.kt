package com.dialysis.app

import android.app.Application
import com.dialysis.app.di.CreateDrinkModule
import com.dialysis.app.di.LoginModule
import com.dialysis.app.di.RegisterModule
import com.dialysis.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DialysisApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DialysisApplication)
            modules(appModule, RegisterModule, LoginModule, CreateDrinkModule)
        }
    }
}
