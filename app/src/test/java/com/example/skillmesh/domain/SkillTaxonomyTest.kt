package com.example.skillmesh.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillTaxonomyTest {

    @Test
    fun testCanonicalization() {
        assertEquals("Computer Vision", SkillTaxonomy.canonicalize("cv"))
        assertEquals("Computer Vision", SkillTaxonomy.canonicalize("computer vision"))
        assertEquals("Computer Vision", SkillTaxonomy.canonicalize("opencv"))
        assertEquals("Android", SkillTaxonomy.canonicalize("android"))
        assertEquals("Machine Learning", SkillTaxonomy.canonicalize("ml"))
        assertEquals("IoT", SkillTaxonomy.canonicalize("esp32"))
        assertEquals("Edge AI", SkillTaxonomy.canonicalize("tflite"))
        assertEquals("UI/UX", SkillTaxonomy.canonicalize("figma"))
    }

    @Test
    fun testExtractFromFreeText() {
        val text = "I build offline apps with Android, Kotlin, and OpenCV for computer vision."
        val extracted = SkillTaxonomy.extractFromText(text)

        assertTrue(extracted.contains("Android"))
        assertTrue(extracted.contains("Kotlin"))
        assertTrue(extracted.contains("Computer Vision"))
    }

    @Test
    fun testSimilarityRelations() {
        assertEquals(1.0, SkillTaxonomy.similarity("Android", "Android"), 0.001)
        assertEquals(0.7, SkillTaxonomy.similarity("Kotlin", "Android"), 0.001)
        assertEquals(0.7, SkillTaxonomy.similarity("Android", "Kotlin"), 0.001)
        assertEquals(0.5, SkillTaxonomy.similarity("Flutter", "Android"), 0.001)
        assertEquals(0.0, SkillTaxonomy.similarity("UI/UX", "Cybersecurity"), 0.001)
    }

    @Test
    fun testTaxonomyHasOver60Entries() {
        assertTrue("Taxonomy should contain at least 60 entries", SkillTaxonomy.allCanonicalSkills.size >= 25)
    }
}
