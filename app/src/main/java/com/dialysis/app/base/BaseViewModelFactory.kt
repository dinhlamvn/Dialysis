package com.dialysis.app.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BaseViewModelFactory<VM : ViewModel>(
    private val clazz: Class<VM>,
    private val block: () -> VM
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(clazz)) {
            @Suppress("UNCHECKED_CAST")
            return block.invoke() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}