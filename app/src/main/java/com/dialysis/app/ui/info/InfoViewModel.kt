package com.dialysis.app.ui.info

import com.dialysis.app.base.BaseViewModel
import com.dialysis.app.data.local.WeightTrackingRepository
import com.dialysis.app.sharepref.UserProfileSharePref
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoViewModel(
    private val userProfileSharePref: UserProfileSharePref,
    private val weightTrackingRepository: WeightTrackingRepository
) : BaseViewModel<InfoState>(InfoState()) {

    val currentStepState = flowOf(InfoState::currentStep).collectStateUI(0)
    val genderState = flowOf(InfoState::gender).collectStateUI(0)
    val weightState = flowOf(InfoState::weight).collectStateUI(50)
    val ageState = flowOf(InfoState::age).collectStateUI(30)
    val nameState = flowOf(InfoState::name).collectStateUI("")
    val phoneState = flowOf(InfoState::phone).collectStateUI("")
    val dialysisStartYearState = flowOf(InfoState::dialysisStartYear).collectStateUI(0)
    val dialysisFreqWeekState = flowOf(InfoState::dialysisFreqWeek).collectStateUI(0)
    val dailyUrineMlState = flowOf(InfoState::dailyUrineMl).collectStateUI(0)

    fun nextStep() = setState { copy(currentStep = currentStep + 1) }

    fun prevStep() = setState { copy(currentStep = currentStep - 1) }

    fun updateGender(gender: Int) = setState { copy(gender = gender) }

    fun updateWeight(weight: Int) = setState { copy(weight = weight) }

    fun updateAge(age: Int) = setState { copy(age = age) }

    fun updateName(name: String) = setState { copy(name = name) }

    fun updatePhone(phone: String) = setState { copy(phone = phone) }

    fun updateDialysisStartYear(year: Int) = setState { copy(dialysisStartYear = year) }

    fun updateDialysisFreqWeek(freq: Int) = setState { copy(dialysisFreqWeek = freq) }

    fun updateDailyUrineMl(ml: Int) = setState { copy(dailyUrineMl = ml) }

    fun saveProfile() {
        getState { state ->
            userProfileSharePref.saveProfile(state)
            viewModelScope.launch(Dispatchers.IO) {
                weightTrackingRepository.saveDailyWeight(weightKg = state.weight.toFloat())
            }
        }
    }
}
