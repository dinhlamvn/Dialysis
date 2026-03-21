package com.dialysis.app.di

import androidx.room.Room
import com.dialysis.app.data.local.AppDatabase
import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.ui.daily.DailyReportViewModel
import com.dialysis.app.ui.drink.create.CreateDrinkViewModel
import com.dialysis.app.ui.home.HomeViewModel
import com.dialysis.app.ui.login.LoginViewModel
import com.dialysis.app.ui.otpverify.OtpVerifyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { NetworkManager.appPublicServices }
    single { NetworkManager.appServices }
    single { AccountSharePref(get()) }
    single {
        Room.databaseBuilder(
            get(),
            AppDatabase::class.java,
            "dialysis.db"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<AppDatabase>().waterEntryDao() }
    single { WaterTrackingRepository(get()) }
}

val RegisterModule = module {
    viewModel { OtpVerifyViewModel(get()) }
}

val LoginModule = module {
    viewModel { LoginViewModel(get()) }
}

val CreateDrinkModule = module {
    viewModel { CreateDrinkViewModel() }
}

val HomeModule = module {
    viewModel { HomeViewModel(get()) }
}

val DailyReportModule = module {
    viewModel { DailyReportViewModel(get()) }
}
