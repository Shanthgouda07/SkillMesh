package com.example.skillmesh.domain

object MatchEngine {
    data class Weights(
        val coverage: Double = 0.65,
        val interest: Double = 0.20,
        val availability: Double = 0.10,
        val experience: Double = 0.05
    )

    fun score(
        req: Requirement,
        p: Profile,
        w: Weights = Weights()
    ): Triple<Int, List<String>, List<String>> {
        val matched = mutableListOf<String>()
        val missing = mutableListOf<String>()
        var coverageSum = 0.0

        for (need in req.requiredSkills) {
            val best = p.skills.maxOfOrNull { SkillTaxonomy.similarity(need, it) } ?: 0.0
            coverageSum += best
            if (best >= 0.5) {
                matched += need
            } else {
                missing += need
            }
        }

        val coverage = if (req.requiredSkills.isEmpty()) 0.0 else coverageSum / req.requiredSkills.size
        val interest = if (req.interests.isEmpty()) 0.0 else
            req.interests.count { i -> p.interests.any { it.equals(i, ignoreCase = true) } }.toDouble() / req.interests.size
        val avail = if (p.available) 1.0 else 0.0
        val exp = when (p.experience) {
            "Advanced" -> 1.0
            "Intermediate" -> 0.7
            else -> 0.4
        }

        val total = w.coverage * coverage + w.interest * interest + w.availability * avail + w.experience * exp
        val finalScore = (total * 100).toInt().coerceIn(0, 100)
        return Triple(finalScore, matched, missing)
    }

    fun rank(
        req: Requirement,
        people: List<Profile>
    ): List<Pair<Profile, Triple<Int, List<String>, List<String>>>> =
        people.map { it to score(req, it) }.sortedByDescending { it.second.first }
}
