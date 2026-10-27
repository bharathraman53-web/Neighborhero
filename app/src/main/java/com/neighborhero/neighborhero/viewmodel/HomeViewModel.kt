package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.data.repository.HelpRequestRepository
import com.neighborhero.neighborhero.data.repository.HelperRepository
import com.neighborhero.neighborhero.data.repository.UserRepository
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val helpRequestRepository: HelpRequestRepository = HelpRequestRepository(),
    private val helperRepository: HelperRepository = HelperRepository()
) : ViewModel() {
    val currentUser = userRepository.currentUser
    val nearbyRequests: StateFlow<List<HelpRequest>> = helpRequestRepository.requests
    val topHelpers: StateFlow<List<HelperProfile>> = helperRepository.helpers
}
