package com.dialysis.app.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.reflect.KProperty1

abstract class BaseViewModel<S : BaseState>(
    initState: S,
    private val stateManager: StateManager<S> = StateManagerReal(initState)
) : ViewModel() {

    protected fun setState(block: S.() -> S) = stateManager.update(block)

    protected fun getState(block: (S) -> Unit) = stateManager.get(block)

    fun <T> flowOf(mapper: (S) -> T) = stateManager.state.map(mapper)

    protected fun <A> flowOf(
        property: KProperty1<S, A>
    ) = stateManager.state.map { state ->
        property.get(state)
    }.distinctUntilChanged()

    protected fun <A, B> flowOf(
        property1: KProperty1<S, A>,
        property2: KProperty1<S, B>
    ) = stateManager.state.map { state ->
        Mapper2(
            property1.get(state),
            property2.get(state)
        )
    }.distinctUntilChanged()

    protected fun <A, B, C> flowOf(
        property1: KProperty1<S, A>,
        property2: KProperty1<S, B>,
        property3: KProperty1<S, C>
    ) = stateManager.state.map { state ->
        Mapper3(
            property1.get(state),
            property2.get(state),
            property3.get(state)
        )
    }.distinctUntilChanged()

    protected fun <A, B, C, D> flowOf(
        property1: KProperty1<S, A>,
        property2: KProperty1<S, B>,
        property3: KProperty1<S, C>,
        property4: KProperty1<S, D>
    ) = stateManager.state.map { state ->
        Mapper4(
            property1.get(state),
            property2.get(state),
            property3.get(state),
            property4.get(state)
        )
    }.distinctUntilChanged()

    protected fun <A, B, C, D, E> flowOf(
        property1: KProperty1<S, A>,
        property2: KProperty1<S, B>,
        property3: KProperty1<S, C>,
        property4: KProperty1<S, D>,
        property5: KProperty1<S, E>
    ) = stateManager.state.map { state ->
        Mapper5(
            property1.get(state),
            property2.get(state),
            property3.get(state),
            property4.get(state),
            property5.get(state)
        )
    }.distinctUntilChanged()

    protected fun <A, B, C, D, E, F> flowOf(
        property1: KProperty1<S, A>,
        property2: KProperty1<S, B>,
        property3: KProperty1<S, C>,
        property4: KProperty1<S, D>,
        property5: KProperty1<S, E>,
        property6: KProperty1<S, F>,
    ) = stateManager.state.map { state ->
        Mapper6(
            property1.get(state),
            property2.get(state),
            property3.get(state),
            property4.get(state),
            property5.get(state),
            property6.get(state),
        )
    }.distinctUntilChanged()

    protected fun <A, B, C, D, E, F, G> flowOf(
        property1: KProperty1<S, A>,
        property2: KProperty1<S, B>,
        property3: KProperty1<S, C>,
        property4: KProperty1<S, D>,
        property5: KProperty1<S, E>,
        property6: KProperty1<S, F>,
        property7: KProperty1<S, G>,
    ) = stateManager.state.map { state ->
        Mapper7(
            property1.get(state),
            property2.get(state),
            property3.get(state),
            property4.get(state),
            property5.get(state),
            property6.get(state),
            property7.get(state),
        )
    }.distinctUntilChanged()

    protected fun <T> Flow<T>.collectStateUI(initValue: T): StateFlow<T> = this.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3_000), initValue
    )
}