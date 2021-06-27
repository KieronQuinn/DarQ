package com.kieronquinn.app.darq.utils.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

inline fun <reified T> instantCombine(vararg flows: Flow<T>) = channelFlow {

    val array = Array(flows.size) {
        false to (null as T?)
    }

    flows.forEachIndexed { index, flow ->
        launch {
            flow.collect { emittedElement ->
                array[index] = true to emittedElement
                send(array.filter { it.first }.map { it.second })
            }
        }
    }
}