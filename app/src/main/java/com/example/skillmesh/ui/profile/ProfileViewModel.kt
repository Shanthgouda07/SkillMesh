package com.example.skillmesh.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skillmesh.ai.LocalAIEngine
import com.example.skillmesh.data.Repository
import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.SkillTaxonomy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ProfileUiState(
    val name: String = "",
    val freeTextBio: String = "",
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val experience: String = "Intermediate",
    val available: Boolean = true,
    val isAnalyzing: Boolean = false,
    val isSaving: Boolean = false,
    val isDemoDataActive: Boolean = false,
    val aiEngineStatusLabel: String = "AI: Deterministic Fallback",
    val feedbackMessage: String? = null
)

class ProfileViewModel(
    private val repository: Repository,
    private val aiEngine: LocalAIEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var profileId: String = UUID.randomUUID().toString()

    init {
        viewModelScope.launch {
            repository.selfProfile.collect { self ->
                if (self != null) {
                    profileId = self.id
                    _uiState.update {
                        it.copy(
                            name = self.name,
                            freeTextBio = self.bio,
                            skills = self.skills,
                            interests = self.interests,
                            experience = self.experience,
                            available = self.available,
                            aiEngineStatusLabel = aiEngine.statusLabel
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            val demoLoaded = repository.isDemoDataLoaded()
            _uiState.update { it.copy(isDemoDataActive = demoLoaded) }
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onBioChanged(newBio: String) {
        _uiState.update { it.copy(freeTextBio = newBio) }
    }

    fun onExperienceChanged(newExp: String) {
        _uiState.update { it.copy(experience = newExp) }
    }

    fun onAvailabilityChanged(isAvail: Boolean) {
        _uiState.update { it.copy(available = isAvail) }
    }

    fun analyzeWithAI() {
        val text = _uiState.value.freeTextBio.trim()
        if (text.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            val extracted = aiEngine.extractProfile(text)
            _uiState.update { current ->
                current.copy(
                    skills = extracted.skills,
                    interests = extracted.interests,
                    experience = extracted.experience,
                    isAnalyzing = false,
                    aiEngineStatusLabel = aiEngine.statusLabel,
                    feedbackMessage = "Skills extracted successfully!"
                )
            }
        }
    }

    fun addSkill(skillInput: String) {
        val canonical = SkillTaxonomy.canonicalize(skillInput) ?: skillInput.trim()
        if (canonical.isNotBlank() && !_uiState.value.skills.contains(canonical)) {
            _uiState.update { it.copy(skills = it.skills + canonical) }
        }
    }

    fun removeSkill(skill: String) {
        _uiState.update { it.copy(skills = it.skills - skill) }
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val current = _uiState.value
            val profile = Profile(
                id = profileId,
                name = current.name.ifBlank { "User ${profileId.take(4)}" },
                bio = current.freeTextBio,
                skills = current.skills,
                interests = current.interests,
                experience = current.experience,
                available = current.available
            )
            repository.saveSelfProfile(profile)
            _uiState.update {
                it.copy(isSaving = false, feedbackMessage = "Profile saved locally!")
            }
        }
    }

    fun toggleDemoMode(enable: Boolean) {
        viewModelScope.launch {
            repository.toggleDemoData(enable)
            _uiState.update {
                it.copy(
                    isDemoDataActive = enable,
                    feedbackMessage = if (enable) "Loaded 10 demo profiles & 3 projects" else "Cleared demo data"
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
