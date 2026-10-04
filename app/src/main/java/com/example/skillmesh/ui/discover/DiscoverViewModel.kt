package com.example.skillmesh.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillmesh.ai.LocalAIEngine
import com.example.skillmesh.data.Repository
import com.example.skillmesh.domain.MatchEngine
import com.example.skillmesh.domain.MatchResult
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.Requirement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DiscoverUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val currentRequirement: Requirement? = null,
    val matchResults: List<MatchResult> = emptyList(),
    val allPeopleCount: Int = 0,
    val selectedProfile: Profile? = null,
    val inviteSuccessMessage: String? = null
)

class DiscoverViewModel(
    private val repository: Repository,
    private val aiEngine: LocalAIEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        // Automatically rank when discoverable profiles change if requirement is present
        viewModelScope.launch {
            repository.discoverableProfiles.collect { people ->
                _uiState.update { it.copy(allPeopleCount = people.size) }
                val req = _uiState.value.currentRequirement
                if (req != null) {
                    performRanking(req, people)
                } else if (_uiState.value.matchResults.isEmpty() && people.isNotEmpty()) {
                    // Show initial list ordered by default availability
                    val defaultReq = Requirement(
                        rawText = "All nearby and demo developers",
                        requiredSkills = emptyList()
                    )
                    performRanking(defaultReq, people)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun search() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }

            // Step 1: AI / Fallback requirement extraction
            val requirement = aiEngine.extractRequirement(query)
            _uiState.update { it.copy(currentRequirement = requirement) }

            // Step 2: Rank against all discoverable profiles
            repository.discoverableProfiles.collect { people ->
                performRanking(requirement, people)
                _uiState.update { it.copy(isSearching = false) }
            }
        }
    }

    private suspend fun performRanking(req: Requirement, people: List<Profile>) {
        val ranked = MatchEngine.rank(req, people)

        val results = ranked.map { (profile, scoreTuple) ->
            val (score, matched, missing) = scoreTuple
            val explanation = aiEngine.explainMatch(req, profile, matched, missing)
            MatchResult(
                profile = profile,
                score = score,
                matchedSkills = matched,
                missingSkills = missing,
                explanation = explanation
            )
        }

        _uiState.update { it.copy(matchResults = results, isSearching = false) }
    }

    fun selectProfile(profile: Profile?) {
        _uiState.update { it.copy(selectedProfile = profile) }
    }

    fun inviteToProject(projectTitle: String) {
        val person = _uiState.value.selectedProfile ?: return
        _uiState.update {
            it.copy(
                inviteSuccessMessage = "Invitation sent to ${person.name} for $projectTitle!",
                selectedProfile = null
            )
        }
    }

    fun clearInviteMessage() {
        _uiState.update { it.copy(inviteSuccessMessage = null) }
    }
}
