package com.neighborhero.neighborhero.ai

import com.neighborhero.neighborhero.data.model.RequestCategory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.RandomAccessFile

/**
 * Comprehensive Unit Test Suite for NeighborHero Image AI (Phase 3).
 * Verifies model assets, input dimensions, preprocessing contracts,
 * assessment rules, unsupported category bypass, and safety guardrails.
 */
class ImageEvidenceClassifierTest {

    private lateinit var classifier: ImageEvidenceClassifier
    private val standardLabels = listOf("groceries", "pet_help", "repairs", "unrelated")

    @Before
    fun setUp() {
        classifier = ImageEvidenceClassifier()
    }

    // --- 1. Model Asset Loading & Header Verification ---

    @Test
    fun testModelAsset_existsInAssetsDirectory() {
        val modelFile = File("src/main/assets/evidence_classifier.tflite")
        val altModelFile = File("app/src/main/assets/evidence_classifier.tflite")
        val targetFile = if (modelFile.exists()) modelFile else altModelFile

        assertTrue("TFLite model file must exist in assets", targetFile.exists())
        assertTrue("TFLite float32 model must be > 9 MB", targetFile.length() > 9 * 1024 * 1024)

        // Verify FlatBuffer header (TFL3 magic bytes at offset 4)
        RandomAccessFile(targetFile, "r").use { raf ->
            raf.seek(4)
            val magic = ByteArray(4)
            raf.readFully(magic)
            val magicStr = String(magic)
            assertEquals("TFL3", magicStr)
        }
    }

    @Test
    fun testLabelsAsset_existsAndContainsAllFourClasses() {
        val labelsFile = File("src/main/assets/labels.txt")
        val altLabelsFile = File("app/src/main/assets/labels.txt")
        val targetFile = if (labelsFile.exists()) labelsFile else altLabelsFile

        assertTrue("Labels file must exist in assets", targetFile.exists())
        val lines = targetFile.readLines().map { it.trim() }.filter { it.isNotEmpty() }
        assertEquals(4, lines.size)
        assertEquals("groceries", lines[0])
        assertEquals("pet_help", lines[1])
        assertEquals("repairs", lines[2])
        assertEquals("unrelated", lines[3])
    }

    // --- 2. Input Dimensions & Preprocessing Specifications ---

    @Test
    fun testInputDimensions_matchMobileNetV2Specification() {
        assertEquals(224, ImageEvidenceClassifier.INPUT_WIDTH)
        assertEquals(224, ImageEvidenceClassifier.INPUT_HEIGHT)
        assertEquals(3, ImageEvidenceClassifier.INPUT_CHANNELS)
        assertEquals(4, ImageEvidenceClassifier.BYTES_PER_FLOAT)

        val expectedBufferSize = 1 * 224 * 224 * 3 * 4 // 602,112 bytes
        assertEquals(602112, expectedBufferSize)
    }

    @Test
    fun testCategorySupport_detectsSupportedAndUnsupportedCategories() {
        // Supported
        assertTrue(classifier.isCategorySupported("Groceries"))
        assertTrue(classifier.isCategorySupported("groceries"))
        assertTrue(classifier.isCategorySupported("Repairs"))
        assertTrue(classifier.isCategorySupported("repairs"))
        assertTrue(classifier.isCategorySupported("Pet Help"))
        assertTrue(classifier.isCategorySupported("pet help"))
        assertTrue(classifier.isCategorySupported("Pet Care")) // Legacy alias
        assertTrue(classifier.isCategorySupported("pet care"))

        // Unsupported (must be bypassed)
        assertFalse(classifier.isCategorySupported("Books"))
        assertFalse(classifier.isCategorySupported("Transport"))
        assertFalse(classifier.isCategorySupported("Other"))
        assertFalse(classifier.isCategorySupported(null))
        assertFalse(classifier.isCategorySupported(""))
    }

    // --- 3. Output Classes & High-Confidence Relevant Assessments ---

    @Test
    fun testEvaluateAssessment_groceriesHighConfidence_likelyRelevant() {
        // [groceries, pet_help, repairs, unrelated]
        val probs = floatArrayOf(0.88f, 0.04f, 0.05f, 0.03f)
        val result = classifier.evaluateAssessment("Groceries", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.LIKELY_RELEVANT, result.status)
        assertEquals("Groceries", result.predictedCategory)
        assertEquals(0.88f, result.confidence, 0.001f)
        assertTrue(result.displayMessage.contains("likely relevant to Groceries"))
        assertFalse(result.isBypassed)
    }

    @Test
    fun testEvaluateAssessment_repairsHighConfidence_likelyRelevant() {
        val probs = floatArrayOf(0.05f, 0.05f, 0.82f, 0.08f)
        val result = classifier.evaluateAssessment("Repairs", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.LIKELY_RELEVANT, result.status)
        assertEquals("Repairs", result.predictedCategory)
        assertEquals(0.82f, result.confidence, 0.001f)
        assertTrue(result.displayMessage.contains("likely relevant to Repairs"))
    }

    @Test
    fun testEvaluateAssessment_petHelpHighConfidence_likelyRelevant() {
        val probs = floatArrayOf(0.06f, 0.79f, 0.05f, 0.10f)
        val result = classifier.evaluateAssessment("Pet Help", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.LIKELY_RELEVANT, result.status)
        assertEquals("Pet Help", result.predictedCategory)
        assertEquals(0.79f, result.confidence, 0.001f)
        assertTrue(result.displayMessage.contains("likely relevant to Pet Help"))
    }

    @Test
    fun testEvaluateAssessment_legacyPetCare_mappedToPetHelp() {
        val probs = floatArrayOf(0.03f, 0.85f, 0.07f, 0.05f)
        val result = classifier.evaluateAssessment("Pet Care", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.LIKELY_RELEVANT, result.status)
        assertEquals("Pet Help", result.predictedCategory)
        assertTrue(result.displayMessage.contains("likely relevant to Pet Care"))
    }

    // --- 4. Unsupported Category Bypass ---

    @Test
    fun testEvaluateAssessment_booksCategory_bypassedWithoutPenalty() {
        val probs = floatArrayOf(0.25f, 0.25f, 0.25f, 0.25f)
        val result = classifier.evaluateAssessment("Books", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.BYPASS_UNSUPPORTED, result.status)
        assertTrue(result.isBypassed)
        assertNull(result.predictedCategory)
        assertTrue(result.displayMessage.contains("Visual AI is not applicable for this category"))
    }

    @Test
    fun testEvaluateAssessment_transportCategory_bypassedWithoutPenalty() {
        val probs = floatArrayOf(0.25f, 0.25f, 0.25f, 0.25f)
        val result = classifier.evaluateAssessment("Transport", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.BYPASS_UNSUPPORTED, result.status)
        assertTrue(result.isBypassed)
        assertTrue(result.displayMessage.contains("Transport"))
    }

    @Test
    fun testEvaluateAssessment_otherCategory_bypassedWithoutPenalty() {
        val probs = floatArrayOf(0.25f, 0.25f, 0.25f, 0.25f)
        val result = classifier.evaluateAssessment("Other", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.BYPASS_UNSUPPORTED, result.status)
        assertTrue(result.isBypassed)
    }

    // --- 5. Ambiguity & Low Confidence Handling ---

    @Test
    fun testEvaluateAssessment_matchingCategoryBelowThreshold_needsReview() {
        // Groceries matches target, but confidence 0.45 is below 0.60
        val probs = floatArrayOf(0.45f, 0.20f, 0.20f, 0.15f)
        val result = classifier.evaluateAssessment("Groceries", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.NEEDS_REVIEW, result.status)
        assertEquals("Groceries", result.predictedCategory)
        assertTrue(result.displayMessage.contains("ambiguous"))
    }

    @Test
    fun testEvaluateAssessment_unrelatedScenery_possibleMismatch() {
        // Unrelated scenery prediction with >= 0.50 confidence
        val probs = floatArrayOf(0.10f, 0.10f, 0.10f, 0.70f)
        val result = classifier.evaluateAssessment("Groceries", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.POSSIBLE_MISMATCH, result.status)
        assertEquals("Unrelated", result.predictedCategory)
        assertTrue(result.displayMessage.contains("unrelated"))
    }

    @Test
    fun testEvaluateAssessment_categoryCrossTalk_possibleMismatch() {
        // User is completing a Groceries task, but photo clearly shows Repairs (wrenches/plumbing)
        val probs = floatArrayOf(0.08f, 0.05f, 0.81f, 0.06f)
        val result = classifier.evaluateAssessment("Groceries", probs, standardLabels)

        assertEquals(EvidenceAssessmentStatus.POSSIBLE_MISMATCH, result.status)
        assertEquals("Repairs", result.predictedCategory)
        assertTrue(result.displayMessage.contains("Photo appears closer to Repairs than Groceries"))
    }

    // --- 6. Safety & Guardrail Invariants ---

    @Test
    fun testSafetyInvariant_neverClaimsVerifiedComplete() {
        // Even with 99.9% confidence, status must be LIKELY_RELEVANT, NEVER "VERIFIED_COMPLETE"
        val probs = floatArrayOf(0.999f, 0.0003f, 0.0003f, 0.0004f)
        val result = classifier.evaluateAssessment("Groceries", probs, standardLabels)

        assertNotEquals("VERIFIED_COMPLETE", result.status.name)
        assertEquals(EvidenceAssessmentStatus.LIKELY_RELEVANT, result.status)
        assertFalse(result.displayMessage.contains("verified complete", ignoreCase = true))
    }

    @Test
    fun testSafetyInvariant_submissionsAreNeverBlockedOnMismatch() {
        val probs = floatArrayOf(0.05f, 0.05f, 0.05f, 0.85f)
        val result = classifier.evaluateAssessment("Repairs", probs, standardLabels)

        // Must give informative feedback, not throw an exception or block submission
        assertEquals(EvidenceAssessmentStatus.POSSIBLE_MISMATCH, result.status)
        assertNotNull(result.displayMessage)
    }

    @Test
    fun testFormatting_classNamesFormattedProperly() {
        assertEquals(RequestCategory.GROCERIES, classifier.formatClassName("groceries"))
        assertEquals(RequestCategory.PET_HELP, classifier.formatClassName("pet_help"))
        assertEquals(RequestCategory.REPAIRS, classifier.formatClassName("repairs"))
        assertEquals("Unrelated", classifier.formatClassName("unrelated"))
    }

    // --- 7. Simplified Three-State AI Matching Status Tests ---

    @Test
    fun testAiMatchStatus_threeSimplifiedStatusesWithoutPercentages() {
        // 1. Matched
        assertEquals("Matched", AiMatchStatus.MATCHED.title)
        assertEquals("The image appears relevant to the requested task.", AiMatchStatus.MATCHED.advisory)
        assertFalse(AiMatchStatus.MATCHED.advisory.contains("%"))

        // 2. Partially Matched
        assertEquals("Partially Matched", AiMatchStatus.PARTIALLY_MATCHED.title)
        assertEquals("The image may be relevant, but needs closer inspection.", AiMatchStatus.PARTIALLY_MATCHED.advisory)
        assertFalse(AiMatchStatus.PARTIALLY_MATCHED.advisory.contains("%"))

        // 3. Not Matched
        assertEquals("Not Matched", AiMatchStatus.NOT_MATCHED.title)
        assertEquals("The image does not appear relevant to the requested task.", AiMatchStatus.NOT_MATCHED.advisory)
        assertFalse(AiMatchStatus.NOT_MATCHED.advisory.contains("%"))
    }

    @Test
    fun testAiMatchStatus_resultMappingAndNoPercentageLeakage() {
        // High confidence match -> MATCHED
        val matchResult = classifier.evaluateAssessment("Groceries", floatArrayOf(0.90f, 0.03f, 0.04f, 0.03f), standardLabels)
        assertEquals(AiMatchStatus.MATCHED, matchResult.matchStatus)
        assertEquals("Matched", matchResult.matchTitle)
        assertEquals("The image appears relevant to the requested task.", matchResult.matchAdvisory)
        assertFalse("displayMessage must not leak percentage numbers", matchResult.displayMessage.contains("%"))

        // Ambiguous match -> PARTIALLY_MATCHED
        val partialResult = classifier.evaluateAssessment("Groceries", floatArrayOf(0.45f, 0.20f, 0.20f, 0.15f), standardLabels)
        assertEquals(AiMatchStatus.PARTIALLY_MATCHED, partialResult.matchStatus)
        assertEquals("Partially Matched", partialResult.matchTitle)
        assertEquals("The image may be relevant, but needs closer inspection.", partialResult.matchAdvisory)
        assertFalse("displayMessage must not leak percentage numbers", partialResult.displayMessage.contains("%"))

        // Mismatched / unrelated -> NOT_MATCHED
        val mismatchResult = classifier.evaluateAssessment("Groceries", floatArrayOf(0.05f, 0.05f, 0.05f, 0.85f), standardLabels)
        assertEquals(AiMatchStatus.NOT_MATCHED, mismatchResult.matchStatus)
        assertEquals("Not Matched", mismatchResult.matchTitle)
        assertEquals("The image does not appear relevant to the requested task.", mismatchResult.matchAdvisory)
        assertFalse("displayMessage must not leak percentage numbers", mismatchResult.displayMessage.contains("%"))
    }
}

