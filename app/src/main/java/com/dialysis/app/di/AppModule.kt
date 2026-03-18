package com.dialysis.app.di

import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.ui.drink.create.CreateDrinkViewModel
import com.dialysis.app.ui.login.LoginViewModel
import com.dialysis.app.ui.otpverify.OtpVerifyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { NetworkManager.appPublicServices }
    single { NetworkManager.appServices }
    single { AccountSharePref(get()) }
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
