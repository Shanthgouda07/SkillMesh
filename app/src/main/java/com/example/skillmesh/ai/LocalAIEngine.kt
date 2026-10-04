package com.example.skillmesh.ai

import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.Requirement
import kotlinx.serialization.Serializable

@Serializable
data class ExtractedProfile(
    val skills: List<String>,
    val interests: List<String>,
    val experience: String
)

enum class AIModelStatus(val label: String) {
    LOADING("AI: Loading..."),
    READY("AI: On-Device (Gemma)"),
    FALLBACK("AI: Deterministic Fallback")
}

interface LocalAIEngine {
    suspend fun extractProfile(freeText: String): ExtractedProfile
    suspend fun extractRequirement(freeText: String): Requirement
    suspend fun explainMatch(req: Requirement, p: Profile, matched: List<String>, missing: List<String>): String
    suspend fun extractTasks(projectDescription: String, requiredSkills: List<String>): List<String>
    val isLocalModelActive: Boolean
    val statusLabel: String
}
