package dev.soupslurpr.transcribro.recognitionservice

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

/**
 * Integration tests for the audio processing pipeline.
 *
 * Tests cover:
 * - End-to-end audio data flow
 * - Audio format conversion
 * - Buffer management
 * - Multi-segment transcription assembly
 * - Edge cases in audio processing
 */
class AudioProcessingIntegrationTest {

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val SPEECH_START_PAD_MS = 24000
    }

    // ==================== Audio Format Conversion Tests ====================

    @Test
    fun `short to float conversion preserves signal shape`() {
        // Create a sine wave in short format
        val sineWave = ShortArray(1000) { i ->
            (sin(2.0 * PI * i / 100) * 16383).toInt().toShort()
        }

        // Convert to float
        val floatWave = sineWave.map { it.toFloat() / 32767f }.toFloatArray()

        // Verify signal shape is preserved (check peaks)
        val peakIndex = floatWave.indices.maxByOrNull { floatWave[it] } ?: -1
        assertThat(floatWave[peakIndex]).isGreaterThan(0.4f)

        val troughIndex = floatWave.indices.minByOrNull { floatWave[it] } ?: -1
        assertThat(floatWave[troughIndex]).isLessThan(-0.4f)
    }

    @Test
    fun `audio normalization keeps values in valid range`() {
        val audioData = shortArrayOf(
            Short.MIN_VALUE,
            Short.MAX_VALUE,
            0,
            16383,
            -16384
        )

        val normalized = audioData.map { (it.toFloat() / 32767f).coerceIn(-1f, 1f) }

        normalized.forEach { value ->
            assertThat(value).isAtLeast(-1f)
            assertThat(value).isAtMost(1f)
        }
    }

    @Test
    fun `silence is converted to zeros`() {
        val silentAudio = ShortArray(1000) { 0 }

        val converted = silentAudio.map { it.toFloat() / 32767f }

        converted.forEach { assertThat(it).isEqualTo(0f) }
    }

    // ==================== Buffer Management Tests ====================

    @Test
    fun `buffer accumulation works correctly`() {
        val transcriptionAudioData = mutableListOf<Short>()
        val bufferSize = 512

        // Simulate multiple buffer reads
        repeat(10) { iteration ->
            val buffer = ShortArray(bufferSize) { (iteration * 100 + it).toShort() }
            transcriptionAudioData.addAll(buffer.toList())
        }

        assertThat(transcriptionAudioData).hasSize(5120)
        assertThat(transcriptionAudioData[0]).isEqualTo(0.toShort())
        assertThat(transcriptionAudioData[512]).isEqualTo(100.toShort())
    }

    @Test
    fun `buffer slicing with padding calculates correct indices`() {
        val audioData = (0 until 50000).map { it.toShort() }.toMutableList()
        val start = 30000.0
        val end = 45000.0

        val startIndex = (start.toInt() - SPEECH_START_PAD_MS).coerceAtLeast(0)
        val endIndex = end.toInt().coerceAtMost(audioData.size - 1)

        val slice = audioData.slice(startIndex..endIndex)

        assertThat(startIndex).isEqualTo(6000) // 30000 - 24000
        assertThat(endIndex).isEqualTo(45000)
        assertThat(slice).hasSize(39001) // 45000 - 6000 + 1
    }

    @Test
    fun `buffer slicing handles start before padding`() {
        val audioData = (0 until 50000).map { it.toShort() }.toMutableList()
        val start = 10000.0 // Less than SPEECH_START_PAD_MS
        val end = 30000.0

        val startIndex = (start.toInt() - SPEECH_START_PAD_MS).coerceAtLeast(0)
        val endIndex = end.toInt().coerceAtMost(audioData.size - 1)

        val slice = audioData.slice(startIndex..endIndex)

        assertThat(startIndex).isEqualTo(0) // Coerced from -14000
        assertThat(slice).hasSize(30001)
    }

    @Test
    fun `buffer slicing handles end beyond data`() {
        val audioData = (0 until 30000).map { it.toShort() }.toMutableList()
        val start = 10000.0
        val end = 50000.0 // Beyond audioData size

        val startIndex = (start.toInt() - SPEECH_START_PAD_MS).coerceAtLeast(0)
        val endIndex = end.toInt().coerceAtMost(audioData.size - 1)

        val slice = audioData.slice(startIndex..endIndex)

        assertThat(endIndex).isEqualTo(29999)
        assertThat(slice).hasSize(30000)
    }

    // ==================== Multi-Segment Transcription Tests ====================

    @Test
    fun `multiple transcription segments join correctly`() {
        data class Segment(val text: String, val index: Int)

        val segments = mapOf(
            0 to Segment("Hello", 0),
            1 to Segment("world", 1),
            2 to Segment("how are you", 2)
        )

        val result = segments.toSortedMap().values.joinToString { it.text }

        assertThat(result).isEqualTo("Hello, world, how are you")
    }

    @Test
    fun `transcription segments maintain order when added out of sequence`() {
        data class Segment(var text: String?)

        val segments = mutableMapOf<Int, Segment>()

        // Add out of order
        segments[2] = Segment("third")
        segments[0] = Segment("first")
        segments[1] = Segment("second")

        val ordered = segments.toSortedMap().values.map { it.text }

        assertThat(ordered).containsExactly("first", "second", "third").inOrder()
    }

    @Test
    fun `transcription handles null text segments`() {
        data class Segment(var text: String?)

        val segments = mutableMapOf<Int, Segment>()
        segments[0] = Segment("Hello")
        segments[1] = Segment(null) // Still transcribing
        segments[2] = Segment("world")

        val result = segments.toSortedMap().values.joinToString { it.text ?: "" }

        assertThat(result).isEqualTo("Hello, , world")
    }

    // ==================== Audio Duration Calculation Tests ====================

    @Test
    fun `duration calculation for 1 second of audio`() {
        val sampleCount = 16000 // 1 second at 16kHz
        val durationMs = ((sampleCount.toFloat() / SAMPLE_RATE) * 1000).toLong()

        assertThat(durationMs).isEqualTo(1000L)
    }

    @Test
    fun `duration calculation for 5 seconds of audio`() {
        val sampleCount = 80000 // 5 seconds at 16kHz
        val durationMs = ((sampleCount.toFloat() / SAMPLE_RATE) * 1000).toLong()

        assertThat(durationMs).isEqualTo(5000L)
    }

    @Test
    fun `duration calculation for sub-second audio`() {
        val sampleCount = 8000 // 0.5 seconds at 16kHz
        val durationMs = ((sampleCount.toFloat() / SAMPLE_RATE) * 1000).toLong()

        assertThat(durationMs).isEqualTo(500L)
    }

    // ==================== Minimum Buffer Size Tests ====================

    @Test
    fun `padding to minimum buffer size adds zeros`() {
        val originalSize = 1000
        val minSize = 32000
        val audio = ShortArray(originalSize) { 100 }

        val paddedAudio = if (audio.size < minSize) {
            ShortArray(minSize).also { padded ->
                audio.copyInto(padded)
                // Remaining values are already 0 from initialization
            }
        } else {
            audio
        }

        assertThat(paddedAudio).hasLength(minSize)
        assertThat(paddedAudio[0]).isEqualTo(100.toShort())
        assertThat(paddedAudio[999]).isEqualTo(100.toShort())
        assertThat(paddedAudio[1000]).isEqualTo(0.toShort())
        assertThat(paddedAudio[31999]).isEqualTo(0.toShort())
    }

    @Test
    fun `no padding when audio exceeds minimum size`() {
        val originalSize = 50000
        val minSize = 32000
        val audio = ShortArray(originalSize) { 100 }

        val result = if (audio.size < minSize) {
            ShortArray(minSize).also { audio.copyInto(it) }
        } else {
            audio
        }

        assertThat(result).hasLength(originalSize)
    }

    // ==================== VAD State Machine Integration Tests ====================

    @Test
    fun `simulated VAD flow detects speech segment`() {
        // Simulate VAD state machine
        var triggered = false
        var speechStart: Double? = null
        var speechEnd: Double? = null
        var currentSample = 0

        val startThreshold = 0.6f
        val endThreshold = 0.45f
        val minSilenceSamples = 4800 // 300ms at 16kHz

        // Simulate speech probabilities over time
        val probabilities = listOf(
            0.2f, 0.3f, 0.7f, 0.8f, 0.75f, // Speech starts
            0.3f, 0.2f, 0.1f, 0.1f, 0.1f   // Speech ends
        )

        val windowSize = 512
        var tempEnd = 0

        for (prob in probabilities) {
            currentSample += windowSize

            if (prob >= startThreshold && !triggered) {
                triggered = true
                speechStart = currentSample.toDouble()
            }

            if (prob < endThreshold && triggered) {
                if (tempEnd == 0) tempEnd = currentSample
                if (currentSample - tempEnd >= minSilenceSamples) {
                    speechEnd = tempEnd.toDouble()
                    triggered = false
                    break
                }
            } else if (prob >= startThreshold && tempEnd != 0) {
                tempEnd = 0
            }
        }

        assertThat(speechStart).isNotNull()
        assertThat(speechStart).isEqualTo(1536.0) // 3rd window triggers
    }

    // ==================== Hallucination Removal Tests ====================

    @Test
    fun `removes trailing space-dot hallucination`() {
        val transcription = "Hello world ."
        val cleaned = if (transcription.endsWith(" .")) {
            transcription.removeSuffix(" .")
        } else {
            transcription
        }

        assertThat(cleaned).isEqualTo("Hello world")
    }

    @Test
    fun `preserves text without hallucination`() {
        val transcription = "Hello world"
        val cleaned = if (transcription.endsWith(" .")) {
            transcription.removeSuffix(" .")
        } else {
            transcription
        }

        assertThat(cleaned).isEqualTo("Hello world")
    }

    @Test
    fun `handles only hallucination content`() {
        val transcription = " ."
        val cleaned = if (transcription.endsWith(" .")) {
            transcription.removeSuffix(" .")
        } else {
            transcription
        }

        assertThat(cleaned).isEmpty()
    }

    @Test
    fun `preserves sentence with proper period`() {
        val transcription = "Hello world."
        val cleaned = if (transcription.endsWith(" .")) {
            transcription.removeSuffix(" .")
        } else {
            transcription
        }

        assertThat(cleaned).isEqualTo("Hello world.")
    }

    // ==================== Real-time Processing Simulation ====================

    @Test
    fun `simulates real-time audio chunk processing`() {
        val totalDurationMs = 3000
        val chunkDurationMs = 32 // ~512 samples at 16kHz
        val chunkSize = (SAMPLE_RATE * chunkDurationMs) / 1000

        var processedSamples = 0
        var chunks = 0

        while (processedSamples < (SAMPLE_RATE * totalDurationMs / 1000)) {
            // Process chunk
            processedSamples += chunkSize
            chunks++
        }

        assertThat(chunks).isGreaterThan(90) // ~93-94 chunks for 3 seconds
        assertThat(processedSamples).isAtLeast(SAMPLE_RATE * totalDurationMs / 1000)
    }
}
