package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.data.repository.HelperRepository
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HelperViewModel(
    private val repository: HelperRepository = RepositoryProvider.helperRepository
) : ViewModel() {
    val helpers: StateFlow<List<HelperProfile>> = repository.helpers
    val leaderboard: StateFlow<List<HelperProfile>> = repository.leaderboard
    val isLoading: StateFlow<Boolean> = repository.isLoading

    private val _selectedHelperId = MutableStateFlow<String?>(null)
    val selectedHelperId: StateFlow<String?> = _selectedHelperId.asStateFlow()

    init {
        refreshHelpers()
        fetchLeaderboard()
    }

    fun refreshHelpers(skill: String? = null) {
        viewModelScope.launch {
            repository.fetchHelpers(skill)
        }
    }

    fun fetchLeaderboard() {
        viewModelScope.launch {
            repository.fetchLeaderboard()
        }
    }

    fun selectHelper(helperId: String) {
        _selectedHelperId.value = helperId
    }

    fun getHelper(helperId: String?): HelperProfile? {
        if (helperId == null) return helpers.value.firstOrNull()
        return repository.getHelperById(helperId) ?: helpers.value.find { it.id == helperId }
    }
}
