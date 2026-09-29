package com.dialysis.app.ui.info

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.ProfileUpdateRequest
import com.dialysis.app.data.network.request.WeightInitialRequest
import com.dialysis.app.sharepref.AccountSharePref
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import java.time.Year
import kotlinx.coroutines.launch

class InfoViewModel(
    private val userProfileSharePref: UserProfileSharePref,
    private val accountSharePref: AccountSharePref,
    private val weightTrackingRepository: WeightTrackingRepository,
    private val networkManager: NetworkManager
) : BaseViewModel<InfoState>(InfoState()) {
    private var hasLoadedInitialData = false

    val currentStepState = collectStateUI(InfoState::currentStep)
    val genderState = collectStateUI(InfoState::gender)
    val weightState = collectStateUI(InfoState::weight)
    val heightState = collectStateUI(InfoState::height)
    val ageState = collectStateUI(InfoState::age)
    val nameState = collectStateUI(InfoState::name)
    val phoneState = collectStateUI(InfoState::phone)
    val dialysisStartYearState = collectStateUI(InfoState::dialysisStartYear)
    val dialysisFreqWeekState = collectStateUI(InfoState::dialysisFreqWeek)
    val dailyUrineMlState = collectStateUI(InfoState::dailyUrineMl)
    val shouldOpenHomeState = collectStateUI(InfoState::shouldOpenHome)

    fun loadInitialData() {
        if (hasLoadedInitialData) return
        hasLoadedInitialData = true
        val profile = userProfileSharePref.getProfile() ?: return
        setState {
            copy(
                gender = profile.gender,
                weight = profile.weight,
                height = profile.height,
                age = profile.age,
                name = profile.name,
                phone = profile.phone,
                dialysisStartYear = profile.dialysisStartYear,
                dialysisFreqWeek = profile.dialysisFreqWeek,
                dailyUrineMl = profile.dailyUrineMl
            )
        }
    }

    fun nextStep() = setState { copy(currentStep = currentStep + 1) }

    fun prevStep() = setState { copy(currentStep = currentStep - 1) }

    fun updateGender(gender: Int) = setState { copy(gender = gender) }

    fun updateWeight(weight: Int) = setState { copy(weight = weight) }

    fun updateHeight(height: Int) = setState { copy(height = height) }

    fun updateAge(age: Int) = setState { copy(age = age) }

    fun updateName(name: String) = setState { copy(name = name) }

    fun updatePhone(phone: String) = setState { copy(phone = phone) }

    fun updateDialysisStartYear(year: Int) = setState { copy(dialysisStartYear = year) }

    fun updateDialysisFreqWeek(freq: Int) = setState { copy(dialysisFreqWeek = freq) }

    fun updateDailyUrineMl(ml: Int) = setState { copy(dailyUrineMl = ml) }

    fun saveProfile() {
        getState { state ->
            if (state.shouldOpenHome) return@getState
            viewModelScope.launch(Dispatchers.IO) {
                weightTrackingRepository.saveDailyWeight(weightKg = state.weight.toFloat())
                userProfileSharePref.saveProfile(state)
                userProfileSharePref.saveDailyWaterGoalMl(calculateLocalDailyWaterGoalMl(state))
                // If user is logged in, send profile update to server with the required fields
                if (accountSharePref.getToken().isNotBlank()) {
                    try {
                        val genderStr = when (state.gender) {
                            1 -> "Male"
                            2 -> "Female"
                            else -> "Other"
                        }
                        val dailyUrine = state.dailyUrineMl
                        val dailyWaterTarget = LOCAL_BASE_DAILY_WATER_GOAL_ML + dailyUrine
                        val dialysisStartYearVal = if (state.dialysisStartYear == 0) Year.now().value else state.dialysisStartYear
                        val request = ProfileUpdateRequest(
                            gender = genderStr,
                            name = state.name,
                            dialysisStartYear = dialysisStartYearVal,
                            dailyWaterTarget = dailyWaterTarget,
                            age = state.age,
                            weight = state.weight,
                            dialysisFreqWeek = state.dialysisFreqWeek,
                            dailyUrineMl = dailyUrine,
                            initialWeight = state.weight
                        )

                        try {
                            networkManager.appServices.updateProfile(request)
                            setState { copy(shouldOpenHome = true) }
                        } catch (e: Exception) {
                            // swallow - do not block UI, navigate home
                            setState { copy(shouldOpenHome = true) }
                        }
                    } catch (e: Exception) {
                        // swallow - do not block UI, navigate home
                        setState { copy(shouldOpenHome = true) }
                    }
                } else {
                    // Not logged in: continue to home immediately
                    setState { copy(shouldOpenHome = true) }
                }
            }
        }
    }

    

    fun consumeOpenHomeEvent() = setState { copy(shouldOpenHome = false) }

    private fun calculateLocalDailyWaterGoalMl(state: InfoState): Int {
        return LOCAL_BASE_DAILY_WATER_GOAL_ML + state.dailyUrineMl
    }

    private companion object {
        private const val LOCAL_BASE_DAILY_WATER_GOAL_ML = 500
    }
}
