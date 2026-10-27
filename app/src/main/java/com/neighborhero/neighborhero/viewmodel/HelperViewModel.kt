package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.data.repository.HelperRepository
import kotlinx.coroutines.flow.StateFlow

class HelperViewModel(
    private val repository: HelperRepository = HelperRepository()
) : ViewModel() {
    val helpers: StateFlow<List<HelperProfile>> = repository.helpers
}
