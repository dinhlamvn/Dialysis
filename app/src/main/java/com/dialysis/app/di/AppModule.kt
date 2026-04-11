package com.dialysis.app.di

import androidx.room.Room
import com.dialysis.app.data.local.AppDatabase
import com.dialysis.app.data.local.WaterTrackingRepository
import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.interceptor.DefaultHeadersInterceptor
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import com.dialysis.app.sync.WaterIntakeSyncScheduler
import com.dialysis.app.ui.daily.DailyReportViewModel
import com.dialysis.app.ui.drink.create.CreateDrinkViewModel
import com.dialysis.app.ui.home.HomeViewModel
import com.dialysis.app.ui.home.tabs.SettingsViewModel
import com.dialysis.app.ui.info.InfoViewModel
import com.dialysis.app.ui.login.LoginViewModel
import com.dialysis.app.ui.otpverify.OtpVerifyViewModel
import com.dialysis.app.ui.register.RegisterViewModel
import com.dialysis.app.ui.weight.WeightViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val NetworkModule = module {
    single<Gson> { GsonBuilder().create() }
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addNetworkInterceptor(DefaultHeadersInterceptor)
            .build()
    }
    single<Retrofit.Builder> {
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(get<Gson>()))
    }
    single { NetworkManager(get(), get(), get(), get()) }
}

val appModule = module {
    single { AccountSharePref(androidContext()) }
    single { UserProfileSharePref(androidContext(), get()) }
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "dialysis.db"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<AppDatabase>().waterEntryDao() }
    single { get<AppDatabase>().weightEntryDao() }
    single { get<AppDatabase>().pendingWaterDeleteDao() }
    single { WaterIntakeSyncScheduler(androidContext()) }
    single { WaterTrackingRepository(get(), get()) }
    single { WeightTrackingRepository(get()) }
}

val RegisterModule = module {
    viewModel { RegisterViewModel(get()) }
    viewModel { OtpVerifyViewModel(get(), get()) }
}

val LoginModule = module {
    viewModel { LoginViewModel(get(), get()) }
}

val CreateDrinkModule = module {
    viewModel { CreateDrinkViewModel() }
}

val HomeModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}

val DailyReportModule = module {
    viewModel { DailyReportViewModel(get(), get()) }
}

val InfoModule = module {
    viewModel { InfoViewModel(get(), get(), get()) }
}

val WeightModule = module {
    viewModel { WeightViewModel(get(), get()) }
}
