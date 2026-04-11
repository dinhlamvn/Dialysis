package com.dialysis.app.ui.info

import androidx.lifecycle.viewModelScope
import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.data.network.NetworkManager
import com.dialysis.app.data.network.request.CalculateWaterTargetRequest
import com.dialysis.app.sharepref.UserProfileSharePref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoViewModel(
    private val userProfileSharePref: UserProfileSharePref,
    private val weightTrackingRepository: WeightTrackingRepository,
    private val networkManager: NetworkManager
) : BaseViewModel<InfoState>(InfoState()) {

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
                val request = CalculateWaterTargetRequest(
                    weight = state.weight.toDouble(),
                    gender = if (state.gender == 1) "male" else "female",
                    height = state.height.toDouble(),
                    activityLevel = "medium",
                    age = state.age
                )
                val result = networkManager.resolve {
                    networkManager.appPublicServices.calculateDailyWaterTarget(request)
                }
                if (result.isSuccess) {
                    userProfileSharePref.saveProfile(state)
                    result.getOrNull()?.dailyWaterTarget?.let { goalMl ->
                        userProfileSharePref.saveDailyWaterGoalMl(goalMl)
                    }
                    setState {
                        copy(
                            isCalculatingGoal = false,
                            calculateGoalStatus = CalculateGoalStatus.Success
                        )
                    }
                } else {
                    val apiMessage = result.exceptionOrNull()?.message?.takeIf { it.isNotBlank() }
                        ?: "Không thể tính mục tiêu nước. Vui lòng thử lại."
                    setState {
                        copy(
                            isCalculatingGoal = false,
                            calculateGoalStatus = CalculateGoalStatus.Failed(apiMessage)
                        )
                    }
                }
            }
        }
    }

    fun retryCalculateGoal() {
        setState { copy(calculateGoalStatus = CalculateGoalStatus.None, isCalculatingGoal = false) }
        saveProfile()
    }

    fun clearCalculateGoalStatus() = setState {
        copy(calculateGoalStatus = CalculateGoalStatus.None)
    }
}
