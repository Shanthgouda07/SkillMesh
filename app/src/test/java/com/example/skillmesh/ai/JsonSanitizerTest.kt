package com.example.skillmesh.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonSanitizerTest {

    @Test
    fun testExtractsFirstJsonBlockFromMarkdownFence() {
        val raw = """
            Here is the requested JSON:
            ```json
            {"skills":["cv","android"],"interests":["robotics"],"experience":"Advanced"}
            ```
            Hope this helps!
        """.trimIndent()

        val parsed = JsonSanitizer.parseExtractedProfile(raw, "Working with cv and android")
        assertNotNull(parsed)
        assertTrue(parsed!!.skills.contains("Computer Vision"))
        assertTrue(parsed.skills.contains("Android"))
        assertEquals("Advanced", parsed.experience)
    }

    @Test
    fun testDropsUnknownSkillsAndMergesDeterministicKeywords() {
        val raw = """
            {"skills":["unknown_skill_xyz","flutter"],"interests":["gaming"],"experience":"Beginner"}
        """.trimIndent()

        val sourceText = "I also use Kotlin daily"
        val parsed = JsonSanitizer.parseExtractedProfile(raw, sourceText)

        assertNotNull(parsed)
        // unknown_skill_xyz should be dropped since it's not in taxonomy
        assertTrue(!parsed!!.skills.contains("unknown_skill_xyz"))
        // flutter should be canonicalized to "Flutter"
        assertTrue(parsed.skills.contains("Flutter"))
        // Kotlin should be merged from source text
        assertTrue(parsed.skills.contains("Kotlin"))
    }

    @Test
    fun testHandlesInvalidJsonGracefully() {
        val raw = "I am a developer who loves Android and Kotlin."
        val parsed = JsonSanitizer.parseExtractedProfile(raw, raw)
        // Should return null for malformed JSON, allowing FallbackEngine to take over
        assertEquals(null, parsed)
    }

    @Test
    fun testParseStringList() {
        val raw = """["Setup camera sensor", "Quantize YOLO weights", "Test latency"]"""
        val list = JsonSanitizer.parseStringList(raw)
        assertEquals(3, list.size)
        assertEquals("Setup camera sensor", list[0])
    }
}
