package com.example.skillmesh.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillmesh.ai.LocalAIEngine
import com.example.skillmesh.data.Repository
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.Project
import com.example.skillmesh.nearby.NearbyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val myProfile: Profile? = null,
    val nearbyCount: Int = 0,
    val isLocalAiActive: Boolean = false,
    val aiStatusLabel: String = "AI: Deterministic Fallback",
    val recentProjects: List<Project> = emptyList(),
    val topPeers: List<Profile> = emptyList(),
    val isDemoDataLoaded: Boolean = false,
    val bannerMessage: String? = null
)

class HomeViewModel(
    private val repository: Repository,
    private val aiEngine: LocalAIEngine,
    private val nearbyManager: NearbyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.selfProfile.collect { self ->
                _uiState.update { it.copy(myProfile = self) }
            }
        }

        viewModelScope.launch {
            nearbyManager.uiState.collect { nearbyState ->
                _uiState.update { it.copy(nearbyCount = nearbyState.discoveredPeers.size) }
            }
        }

        viewModelScope.launch {
            repository.projects.collect { projects ->
                _uiState.update { it.copy(recentProjects = projects.take(3)) }
            }
        }

        viewModelScope.launch {
            repository.discoverableProfiles.collect { people ->
                _uiState.update {
                    it.copy(
                        topPeers = people.take(4),
                        isDemoDataLoaded = repository.isDemoDataLoaded(),
                        isLocalAiActive = aiEngine.isLocalModelActive,
                        aiStatusLabel = aiEngine.statusLabel
                    )
                }
            }
        }
    }

    fun toggleDemoMode() {
        viewModelScope.launch {
            val currentState = repository.isDemoDataLoaded()
            val newState = !currentState
            repository.toggleDemoData(newState)
            _uiState.update {
                it.copy(
                    isDemoDataLoaded = newState,
                    bannerMessage = if (newState) "Demo Mode Enabled (10 people, 3 projects loaded)" else "Demo Mode Disabled"
                )
            }
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(bannerMessage = null) }
    }
}
