package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskViewModel : ViewModel() {
    private val _taskStarted = MutableStateFlow(false)
    val taskStarted: StateFlow<Boolean> = _taskStarted.asStateFlow()

    private val _taskSubmitted = MutableStateFlow(false)
    val taskSubmitted: StateFlow<Boolean> = _taskSubmitted.asStateFlow()

    private val _taskVerified = MutableStateFlow(false)
    val taskVerified: StateFlow<Boolean> = _taskVerified.asStateFlow()

    fun confirmStart() {
        _taskStarted.value = true
    }

    fun submitCompletion(notes: String) {
        _taskSubmitted.value = true
    }

    fun verifyCompletion(rating: Int, feedback: String) {
        _taskVerified.value = true
    }
}
