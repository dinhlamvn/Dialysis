package com.dialysis.app.ui.drink.create

import com.dialysis.app.base.BaseState

data class CreateDrinkState(
    val drinkName: String = "",
    val selectedId: String = "200_ml",
    val showCustomInputSheet: Boolean = false,
    val customInput: String = "",
    val customMl: Int? = null,
    val selectedTimeText: String = ""
) : BaseState
