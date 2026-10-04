package com.example.skillmesh.ui.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillmesh.data.Repository
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.nearby.NearbyManager
import com.example.skillmesh.nearby.NearbyUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NearbyViewModel(
    private val nearbyManager: NearbyManager,
    private val repository: Repository
) : ViewModel() {

    val uiState: StateFlow<NearbyUiState> = nearbyManager.uiState

    val nearbyProfiles: StateFlow<List<Profile>> = repository.nearbyProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startScanning() {
        nearbyManager.start()
    }

    fun stopScanning() {
        nearbyManager.stop()
    }

    fun approveConnection(endpointId: String) {
        nearbyManager.approveConnection(endpointId)
    }

    fun rejectConnection(endpointId: String) {
        nearbyManager.rejectConnection(endpointId)
    }

    fun clearStatusMessage() {
        nearbyManager.clearStatusMessage()
    }
}
