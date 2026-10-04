package com.example.skillmesh.ai

import com.example.skillmesh.domain.Profile
import com.example.skillmesh.domain.Requirement
import com.example.skillmesh.domain.SkillTaxonomy

object FallbackEngine : LocalAIEngine {

    override val isLocalModelActive: Boolean = false
    override val statusLabel: String = "AI: Deterministic Fallback"

    override suspend fun extractProfile(freeText: String): ExtractedProfile {
        val extractedSkills = SkillTaxonomy.extractFromText(freeText)

        val potentialInterests = listOf(
            "Robotics", "Open Source", "Edge Computing", "Smart Cities",
            "Hardware Prototyping", "Design Systems", "Distributed Systems",
            "Civic Tech", "Decentralized Web", "Privacy", "Climate Tech",
            "Autonomous Drones", "Developer Tools", "Health Tech", "Wearables"
        )
        val extractedInterests = potentialInterests.filter { interest ->
            freeText.contains(interest, ignoreCase = true)
        }

        val lower = freeText.lowercase()
        val experience = when {
            lower.contains("senior") || lower.contains("lead") || lower.contains("architect") ||
                    lower.contains("advanced") || lower.contains("years") || lower.contains("expert") -> "Advanced"
            lower.contains("beginner") || lower.contains("student") || lower.contains("junior") ||
                    lower.contains("fresher") || lower.contains("novice") -> "Beginner"
            else -> "Intermediate"
        }

        return ExtractedProfile(
            skills = extractedSkills,
            interests = if (extractedInterests.isNotEmpty()) extractedInterests else listOf("Offline Tech", "Hackathons"),
            experience = experience
        )
    }

    override suspend fun extractRequirement(freeText: String): Requirement {
        val extractedSkills = SkillTaxonomy.extractFromText(freeText)
        val potentialInterests = listOf(
            "Robotics", "Open Source", "Edge Computing", "Privacy", "Health Tech", "IoT"
        )
        val matchedInterests = potentialInterests.filter { freeText.contains(it, ignoreCase = true) }

        return Requirement(
            rawText = freeText,
            requiredSkills = extractedSkills,
            interests = matchedInterests
        )
    }

    override suspend fun explainMatch(
        req: Requirement,
        p: Profile,
        matched: List<String>,
        missing: List<String>
    ): String {
        return when {
            matched.isNotEmpty() && missing.isEmpty() -> {
                "Strong match: has ${matched.joinToString(", ")}. Available and aligned with your team."
            }
            matched.isNotEmpty() && missing.isNotEmpty() -> {
                "Solid match: brings ${matched.joinToString(", ")}. Missing: ${missing.joinToString(", ")}."
            }
            matched.isEmpty() && missing.isNotEmpty() -> {
                "Low alignment: lacks primary requested skills (${missing.joinToString(", ")})."
            }
            else -> {
                "Profile aligned with current team requirements and experience level (${p.experience})."
            }
        }
    }

    override suspend fun extractTasks(
        projectDescription: String,
        requiredSkills: List<String>
    ): List<String> {
        val tasks = mutableListOf<String>()

        if (requiredSkills.isNotEmpty()) {
            requiredSkills.take(3).forEach { skill ->
                tasks.add("Design and implement the core $skill pipeline")
            }
        } else {
            tasks.add("Define project specifications and system architecture")
            tasks.add("Implement MVP client interfaces")
        }

        tasks.add("Benchmark latency, memory, and offline performance")
        tasks.add("Conduct end-to-end integration test over peer network")

        return tasks.distinct()
    }
}
