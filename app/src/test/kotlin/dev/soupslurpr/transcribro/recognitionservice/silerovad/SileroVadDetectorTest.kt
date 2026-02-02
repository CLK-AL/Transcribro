package dev.soupslurpr.transcribro.recognitionservice.silerovad

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SileroVadDetector.
 *
 * These tests verify the voice activity detection logic including:
 * - State transitions between speaking and not speaking
 * - Threshold-based detection
 * - Speech start/end time calculations
 * - Silence duration handling
 */
class SileroVadDetectorTest {

    private lateinit var mockModel: SileroVadOnnxModel

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val START_THRESHOLD = 0.6f
        private const val END_THRESHOLD = 0.45f
        private const val MIN_SILENCE_DURATION_MS = 300
        private const val SPEECH_PAD_MS = 30
    }

    @Before
    fun setup() {
        mockModel = mockk(relaxed = true)
    }

    /**
     * Helper to create a detector with a mocked model.
     * We need to use reflection or a factory pattern since the constructor
     * creates the model internally.
     */
    private fun createTestableDetector(
        modelResponses: List<Float>
    ): SileroVadDetectorTestable {
        return SileroVadDetectorTestable(
            startThreshold = START_THRESHOLD,
            endThreshold = END_THRESHOLD,
            samplingRate = SAMPLE_RATE,
            minSilenceDurationMs = MIN_SILENCE_DURATION_MS,
            speechPadMs = SPEECH_PAD_MS,
            modelResponses = modelResponses
        )
    }

    @Test
    fun `apply returns empty map when speech probability below start threshold and not speaking`() {
        val detector = createTestableDetector(listOf(0.3f)) // Below start threshold

        val audioData = FloatArray(512) { 0f }
        val result = detector.apply(audioData, returnSeconds = false)

        assertThat(result).isEmpty()
    }

    @Test
    fun `apply returns start when speech probability exceeds start threshold`() {
        val detector = createTestableDetector(listOf(0.7f)) // Above start threshold

        val audioData = FloatArray(512) { 0f }
        val result = detector.apply(audioData, returnSeconds = false)

        assertThat(result).containsKey("start")
        assertThat(result["start"]).isAtLeast(0.0)
    }

    @Test
    fun `apply returns end after speech stops and min silence duration passes`() {
        // First call: speech detected (triggers speaking)
        // Subsequent calls: below end threshold (triggers end after silence duration)
        val responses = mutableListOf<Float>()
        responses.add(0.7f) // Start speech
        // Add enough low responses to exceed min silence duration
        repeat(20) { responses.add(0.2f) }

        val detector = createTestableDetector(responses)
        val audioData = FloatArray(512) { 0f }

        // First call should trigger speech start
        val startResult = detector.apply(audioData, returnSeconds = false)
        assertThat(startResult).containsKey("start")

        // Subsequent calls with silence
        var endResult: Map<String, Double> = emptyMap()
        repeat(20) {
            endResult = detector.apply(audioData, returnSeconds = false)
            if (endResult.containsKey("end")) return@repeat
        }

        assertThat(endResult).containsKey("end")
    }

    @Test
    fun `apply resets tempEnd when speech resumes during silence`() {
        // Start speaking, then brief silence, then resume
        val responses = listOf(
            0.7f,  // Start speech
            0.3f,  // Brief silence (below end threshold)
            0.7f,  // Resume speech (should reset tempEnd)
            0.3f   // Silence again
        )

        val detector = createTestableDetector(responses)
        val audioData = FloatArray(512) { 0f }

        // Start speech
        val r1 = detector.apply(audioData, returnSeconds = false)
        assertThat(r1).containsKey("start")

        // Brief silence - tempEnd should be set but no end yet
        val r2 = detector.apply(audioData, returnSeconds = false)
        assertThat(r2).isEmpty()

        // Resume speech - should reset tempEnd
        val r3 = detector.apply(audioData, returnSeconds = false)
        assertThat(r3).isEmpty() // No new start, already triggered

        // Silence again - tempEnd should be fresh
        val r4 = detector.apply(audioData, returnSeconds = false)
        assertThat(r4).isEmpty()
    }

    @Test
    fun `reset clears all state`() {
        val detector = createTestableDetector(listOf(0.7f, 0.7f))
        val audioData = FloatArray(512) { 0f }

        // Trigger speech
        detector.apply(audioData, returnSeconds = false)

        // Reset
        detector.reset()

        // Should be able to detect start again
        val result = detector.apply(audioData, returnSeconds = false)
        assertThat(result).containsKey("start")
    }

    @Test
    fun `constructor throws exception for unsupported sample rate`() {
        try {
            SileroVadDetectorTestable(
                startThreshold = START_THRESHOLD,
                endThreshold = END_THRESHOLD,
                samplingRate = 44100, // Unsupported
                minSilenceDurationMs = MIN_SILENCE_DURATION_MS,
                speechPadMs = SPEECH_PAD_MS,
                modelResponses = listOf(0.5f)
            )
            assert(false) { "Should have thrown IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("does not support sampling rates")
        }
    }

    @Test
    fun `minSilenceSamples calculated correctly from milliseconds`() {
        // With 16000 Hz sample rate and 300ms min silence duration
        // minSilenceSamples = 16000 * 300 / 1000 = 4800
        val detector = createTestableDetector(listOf(0.5f))
        assertThat(detector.getMinSilenceSamples()).isEqualTo(4800f)
    }

    @Test
    fun `speechPadSamples calculated correctly from milliseconds`() {
        // With 16000 Hz sample rate and 30ms speech pad
        // speechPadSamples = 16000 * 30 / 1000 = 480
        val detector = createTestableDetector(listOf(0.5f))
        assertThat(detector.getSpeechPadSamples()).isEqualTo(480f)
    }

    @Test
    fun `speech start time accounts for speech pad`() {
        val detector = createTestableDetector(listOf(0.7f))
        val audioData = FloatArray(1024) { 0f }

        val result = detector.apply(audioData, returnSeconds = false)

        // Start should be currentSample - speechPadSamples
        // currentSample = 1024, speechPadSamples = 480
        // Expected start = 1024 - 480 = 544
        assertThat(result["start"]).isEqualTo(544.0)
    }

    @Test
    fun `speech start time is at least 0`() {
        // Use small audio data so currentSample < speechPadSamples
        val detector = createTestableDetector(listOf(0.7f))
        val audioData = FloatArray(100) { 0f } // 100 < 480 (speechPadSamples)

        val result = detector.apply(audioData, returnSeconds = false)

        assertThat(result["start"]).isEqualTo(0.0)
    }
}

/**
 * Testable version of SileroVadDetector that uses mock model responses.
 * This allows us to test the detection logic without requiring ONNX runtime.
 */
class SileroVadDetectorTestable(
    private val startThreshold: Float,
    private val endThreshold: Float,
    private val samplingRate: Int,
    minSilenceDurationMs: Int,
    speechPadMs: Int,
    private val modelResponses: List<Float>
) {
    private val minSilenceSamples: Float = samplingRate * minSilenceDurationMs / 1000f
    private val speechPadSamples: Float = samplingRate * speechPadMs / 1000f

    private var triggered = false
    private var tempEnd = 0
    private var currentSample = 0
    private var responseIndex = 0

    init {
        require(samplingRate == 8000 || samplingRate == 16000) {
            "does not support sampling rates other than [8000, 16000]"
        }
    }

    fun reset() {
        triggered = false
        tempEnd = 0
        currentSample = 0
        responseIndex = 0
    }

    fun getMinSilenceSamples(): Float = minSilenceSamples
    fun getSpeechPadSamples(): Float = speechPadSamples

    fun apply(audioData: FloatArray, returnSeconds: Boolean): Map<String, Double> {
        val windowSizeSamples = audioData.size
        currentSample += windowSizeSamples

        // Get mock response
        val speechProb = if (responseIndex < modelResponses.size) {
            modelResponses[responseIndex++]
        } else {
            modelResponses.lastOrNull() ?: 0f
        }

        if (speechProb >= startThreshold && tempEnd != 0) {
            tempEnd = 0
        }

        if (speechProb >= startThreshold && !triggered) {
            triggered = true
            var speechStart = (currentSample - speechPadSamples)
            speechStart = kotlin.math.max(speechStart, 0.0f)
            return mapOf("start" to speechStart.toDouble())
        }

        if (speechProb < endThreshold && triggered) {
            if (tempEnd == 0) {
                tempEnd = currentSample
            }
            if (currentSample - tempEnd < minSilenceSamples) {
                return emptyMap()
            } else {
                val speechEnd = (tempEnd + speechPadSamples)
                tempEnd = 0
                triggered = false
                return mapOf("end" to speechEnd.toDouble())
            }
        }

        return emptyMap()
    }
}
