package com.dialysis.app.ui.home

import com.dialysis.app.base.BaseState

data class HomeDrinkItemState(
    val amount: String,
    val name: String,
    val time: String,
)

data class HomeState(
    val drinks: List<HomeDrinkItemState> = emptyList(),
    val showDrinkListSheet: Boolean = false,
    val selectedDrinkName: String? = null,
) : BaseState
