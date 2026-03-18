package com.dialysis.app.ui.drink.create

import com.dialysis.app.base.BaseViewModel

class CreateDrinkViewModel : BaseViewModel<CreateDrinkState>(CreateDrinkState()) {

    val drinkNameState = flowOf(CreateDrinkState::drinkName).collectStateUI("")
    val selectedIdState = flowOf(CreateDrinkState::selectedId).collectStateUI("200_ml")
    val showCustomInputSheetState =
        flowOf(CreateDrinkState::showCustomInputSheet).collectStateUI(false)
    val customInputState = flowOf(CreateDrinkState::customInput).collectStateUI("")
    val customMlState = flowOf(CreateDrinkState::customMl).collectStateUI(null)
    val selectedTimeTextState = flowOf(CreateDrinkState::selectedTimeText).collectStateUI("")

    fun updateDrinkName(value: String) = setState {
        copy(drinkName = value)
    }

    fun updateSelectedId(value: String) = setState {
        copy(selectedId = value)
    }

    fun updateShowCustomInputSheet(value: Boolean) = setState {
        copy(showCustomInputSheet = value)
    }

    fun updateCustomInput(value: String) = setState {
        copy(customInput = value)
    }

    fun applyCustomMl(value: Int) = setState {
        copy(
            customMl = value,
            selectedId = "custom",
            showCustomInputSheet = false
        )
    }

    fun updateSelectedTimeText(value: String) = setState {
        copy(selectedTimeText = value)
    }

    fun resetForm(defaultTimeText: String) = setState {
        copy(
            selectedId = "200_ml",
            showCustomInputSheet = false,
            customInput = "",
            customMl = null,
            selectedTimeText = defaultTimeText
        )
    }
}
