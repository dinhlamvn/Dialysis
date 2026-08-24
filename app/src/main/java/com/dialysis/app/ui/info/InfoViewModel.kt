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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val isCalculatingGoalState = collectStateUI(InfoState::isCalculatingGoal)
    val calculateGoalStatusState = collectStateUI(InfoState::calculateGoalStatus)

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
            if (state.calculateGoalStatus is CalculateGoalStatus.Success) return@getState
            if (state.isCalculatingGoal) return@getState
            setState {
                copy(
                    isCalculatingGoal = true,
                    calculateGoalStatus = CalculateGoalStatus.None
                )
            }
            viewModelScope.launch(Dispatchers.IO) {
                weightTrackingRepository.saveDailyWeight(weightKg = state.weight.toFloat())
                if (accountSharePref.getToken().isBlank()) {
                    userProfileSharePref.saveProfile(state)
                    userProfileSharePref.saveDailyWaterGoalMl(calculateLocalDailyWaterGoalMl(state))
                    setState {
                        copy(
                            isCalculatingGoal = false,
                            calculateGoalStatus = CalculateGoalStatus.Success
                        )
                    }
                    return@launch
                }

                val initialWeightResult = networkManager.resolveNullable {
                    networkManager.appServices.updateInitialWeight(
                        WeightInitialRequest(
                            weight = state.weight.toDouble(),
                            date = formatApiDate(System.currentTimeMillis()),
                            note = ""
                        )
                    )
                }
                if (initialWeightResult.isSuccess) {
                    val profileResult = networkManager.resolveNullable {
                        networkManager.appServices.updateProfile(
                            ProfileUpdateRequest(
                                gender = if (state.gender == 1) "Male" else "Female",
                                name = state.name,
                                dialysisStartYear = state.dialysisStartYear,
                                dailyWaterTarget = calculateLocalDailyWaterGoalMl(state),
                                age = state.age,
                                weight = state.weight,
                                dialysisFreqWeek = state.dialysisFreqWeek,
                                dailyUrineMl = state.dailyUrineMl,
                                initialWeight = state.weight
                            )
                        )
                    }
                    if (profileResult.isSuccess) {
                        userProfileSharePref.saveProfile(state)
                        val dailyWaterGoalMl = profileResult.getOrNull()?.dailyWaterTarget
                            ?.takeIf { it > 0 }
                            ?: calculateLocalDailyWaterGoalMl(state)
                        userProfileSharePref.saveDailyWaterGoalMl(dailyWaterGoalMl)
                        setState {
                            copy(
                                isCalculatingGoal = false,
                                calculateGoalStatus = CalculateGoalStatus.Success
                            )
                        }
                    } else {
                        setState {
                            copy(
                                isCalculatingGoal = false,
                                calculateGoalStatus = failedStatus(profileResult)
                            )
                        }
                    }
                } else {
                    setState {
                        copy(
                            isCalculatingGoal = false,
                            calculateGoalStatus = failedStatus(initialWeightResult)
                        )
                    }
                }
            }
        }
    }

    private fun failedStatus(result: Result<*>): CalculateGoalStatus {
        val apiMessage = result.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
            ?: "Không thể lưu thông tin. Vui lòng thử lại."
        return CalculateGoalStatus.Failed(apiMessage)
    }

    fun retryCalculateGoal() {
        setState { copy(calculateGoalStatus = CalculateGoalStatus.None, isCalculatingGoal = false) }
        saveProfile()
    }

    fun clearCalculateGoalStatus() = setState {
        copy(calculateGoalStatus = CalculateGoalStatus.None)
    }

    private fun calculateLocalDailyWaterGoalMl(state: InfoState): Int {
        return LOCAL_BASE_DAILY_WATER_GOAL_ML + state.dailyUrineMl
    }

    private fun formatApiDate(timeMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timeMillis))

    private companion object {
        private const val LOCAL_BASE_DAILY_WATER_GOAL_ML = 500
    }
}
