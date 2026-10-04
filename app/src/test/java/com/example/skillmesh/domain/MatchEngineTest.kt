package com.example.skillmesh.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchEngineTest {

    private val sampleProfile = Profile(
        id = "p-1",
        name = "Ananya",
        bio = "Mobile & Vision engineer",
        skills = listOf("Android", "Computer Vision", "Kotlin"),
        interests = listOf("Robotics", "Open Source"),
        experience = "Advanced",
        available = true
    )

    @Test
    fun testFullCoverageGivesHighScore() {
        val req = Requirement(
            rawText = "Need Android and Computer Vision",
            requiredSkills = listOf("Android", "Computer Vision"),
            interests = listOf("Robotics")
        )

        val (score, matched, missing) = MatchEngine.score(req, sampleProfile)

        // 0.65 * 1.0 + 0.20 * 1.0 + 0.10 * 1.0 + 0.05 * 1.0 = 1.0 -> 100%
        assertEquals(100, score)
        assertEquals(listOf("Android", "Computer Vision"), matched)
        assertTrue(missing.isEmpty())
    }

    @Test
    fun testZeroOverlapGivesLowScore() {
        val req = Requirement(
            rawText = "Need Rust and Cybersecurity",
            requiredSkills = listOf("Rust", "Cybersecurity"),
            interests = listOf("Crypto")
        )

        val (score, matched, missing) = MatchEngine.score(req, sampleProfile)

        // Only availability (0.10) + experience (0.05) = 15%
        assertEquals(15, score)
        assertTrue(matched.isEmpty())
        assertEquals(listOf("Rust", "Cybersecurity"), missing)
    }

    @Test
    fun testRelatedSkillPartialCreditWorks() {
        // Person knows Kotlin, requirement asks for Android. Kotlin -> Android similarity is 0.7.
        val kotlinDev = Profile(
            id = "p-2",
            name = "Kavita",
            bio = "Kotlin enthusiast",
            skills = listOf("Kotlin"),
            interests = emptyList(),
            experience = "Intermediate",
            available = true
        )

        val req = Requirement(
            rawText = "Need Android",
            requiredSkills = listOf("Android")
        )

        val (score, matched, missing) = MatchEngine.score(req, kotlinDev)

        // coverage = 0.7, interest = 0, avail = 1.0 (0.10), exp = 0.7 (0.05 * 0.7 = 0.035)
        // 0.65 * 0.7 (0.455) + 0.10 + 0.035 = 0.59 -> ~59%
        assertTrue("Score should reflect partial credit, got $score", score in 55..65)
        // similarity 0.7 >= 0.5 so it is counted in matched
        assertTrue("Android should be in matched due to Kotlin similarity >= 0.5", matched.contains("Android"))
        assertTrue(missing.isEmpty())
    }

    @Test
    fun testUnavailablePersonIsPenalized() {
        val busyProfile = sampleProfile.copy(available = false)
        val req = Requirement(
            rawText = "Need Android",
            requiredSkills = listOf("Android")
        )

        val availableScore = MatchEngine.score(req, sampleProfile).first
        val busyScore = MatchEngine.score(req, busyProfile).first

        assertEquals(10, availableScore - busyScore)
    }

    @Test
    fun testEmptyRequirementHandledGracefully() {
        val req = Requirement(
            rawText = "",
            requiredSkills = emptyList(),
            interests = emptyList()
        )

        val (score, matched, missing) = MatchEngine.score(req, sampleProfile)

        assertTrue(score >= 0)
        assertTrue(matched.isEmpty())
        assertTrue(missing.isEmpty())
    }

    @Test
    fun testRankingOrdersByBestMatch() {
        val personA = sampleProfile // Has Android & CV
        val personB = Profile(
            id = "p-b",
            name = "Bob",
            bio = "Backend dev",
            skills = listOf("Node.js", "SQL"),
            interests = emptyList(),
            experience = "Intermediate",
            available = true
        )

        val req = Requirement(
            rawText = "Need Android dev with Computer Vision",
            requiredSkills = listOf("Android", "Computer Vision")
        )

        val ranked = MatchEngine.rank(req, listOf(personB, personA))
        assertEquals("Ananya", ranked.first().first.name)
        assertTrue(ranked.first().second.first > ranked.last().second.first)
    }

    @Test
    fun testDemoStoryQueryReturnsAnanyaFirstWithHighMatch() = kotlinx.coroutines.runBlocking {
        val req = com.example.skillmesh.ai.FallbackEngine.extractRequirement("Android developer with computer vision")
        val ranked = MatchEngine.rank(req, com.example.skillmesh.demo.DemoData.people)
        val top = ranked.first()

        assertEquals("Ananya Sharma", top.first.name)
        val score = top.second.first
        // Required coverage: 0.65 * 1.0 (Android + CV) = 0.65.
        // Availability: 0.10 * 1.0 = 0.10.
        // Experience: 0.05 * 1.0 (Advanced) = 0.05.
        // Total = 0.80 -> 80% without specific interest, and with interests up to 100%.
        assertTrue("Score for Ananya should be >= 80%, got $score", score >= 80)
        assertTrue(top.second.second.contains("Android"))
        assertTrue(top.second.second.contains("Computer Vision"))
    }
}
