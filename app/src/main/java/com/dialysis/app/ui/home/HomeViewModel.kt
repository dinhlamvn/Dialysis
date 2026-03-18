package com.dialysis.app.ui.home

import com.dialysis.app.base.BaseViewModel

class HomeViewModel : BaseViewModel<HomeState>(HomeState()) {

    val drinksState = flowOf(HomeState::drinks).collectStateUI(emptyList())
    val showDrinkListSheetState = flowOf(HomeState::showDrinkListSheet).collectStateUI(false)
    val selectedDrinkNameState = flowOf(HomeState::selectedDrinkName).collectStateUI(null)

    fun openDrinkListSheet() = setState {
        copy(showDrinkListSheet = true)
    }

    fun closeDrinkListSheet() = setState {
        copy(showDrinkListSheet = false)
    }

    fun onDrinkSelected(drinkName: String) = setState {
        copy(
            showDrinkListSheet = false,
            selectedDrinkName = drinkName
        )
    }

    fun dismissCreateDrinkSheet() = setState {
        copy(selectedDrinkName = null)
    }

    fun addDrink(name: String, amount: String, time: String) = setState {
        copy(
            drinks = listOf(
                HomeDrinkItemState(
                    amount = amount,
                    name = name,
                    time = time
                )
            ) + drinks,
            selectedDrinkName = null
        )
    }
}
