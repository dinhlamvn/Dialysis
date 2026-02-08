package com.dialysis.app.base

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import java.util.concurrent.Executors

interface BaseState

interface StateManager<S : BaseState> {
    val state: Flow<S>
    fun update(block: S.() -> S)
    fun get(block: (S) -> Unit)
}

class StateManagerReal<S : BaseState>(initState: S) : StateManager<S> {

    private val _stateFlow = MutableStateFlow<S>(initState)

    override val state: Flow<S>
        get() = _stateFlow.asSharedFlow()

    private val stateScope = CoroutineScope(
        Executors.newSingleThreadExecutor()
            .asCoroutineDispatcher() + CoroutineName("StateScope") + SupervisorJob()
    )

    private val setStateChannel = Channel<S.() -> S>(Channel.UNLIMITED)
    private val getStateChannel = Channel<(S) -> Unit>(Channel.UNLIMITED)

    init {
        stateScope.launch(Dispatchers.IO) {
            while (isActive) {
                select {
                    setStateChannel.onReceive { block ->
                        val newState = block.invoke(_stateFlow.value)
                        if (newState != _stateFlow.value) {
                            _stateFlow.emit(newState)
                        }
                    }

                    getStateChannel.onReceive { block ->
                        block.invoke(_stateFlow.value)
                    }
                }
            }
        }
    }

    override fun update(block: S.() -> S) {
        setStateChannel.trySend(block)
    }

    override fun get(block: (S) -> Unit) {
        getStateChannel.trySend(block)
    }
}