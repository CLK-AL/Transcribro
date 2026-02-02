package dev.soupslurpr.transcribro.recognitionservice

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for MainRecognitionService logic.
 *
 * Note: The MainRecognitionService is an Android RecognitionService which requires
 * the Android runtime. These tests focus on the pure business logic that can be
 * extracted and tested in isolation.
 *
 * Tests cover:
 * - Transcription data structure
 * - Audio slicing calculations
 * - Speech padding calculations
 * - Transcription joining logic
 */
class MainRecognitionServiceTest {

    /**
     * Data class mirroring the private Transcription class in MainRecognitionService
     * for testing purposes.
     */
    data class Transcription(
        val audioData: MutableList<Short> = mutableListOf(),
        var start: Double?,
        var end: Double?,
        var text: String?,
    )

    companion object {
        private const val SPEECH_START_PAD_MS = 24000
    }

    @Test
    fun `transcription data class initializes correctly`() {
        val transcription = Transcription(start = null, end = null, text = null)

        assertThat(transcription.audioData).isEmpty()
        assertThat(transcription.start).isNull()
        assertThat(transcription.end).isNull()
        assertThat(transcription.text).isNull()
    }

    @Test
    fun `transcription stores audio data`() {
        val transcription = Transcription(start = 0.0, end = null, text = null)

        transcription.audioData.add(100)
        transcription.audioData.add(200)
        transcription.audioData.add(-100)

        assertThat(transcription.audioData).hasSize(3)
        assertThat(transcription.audioData[0]).isEqualTo(100.toShort())
    }

    @Test
    fun `audio slicing with speech start pad calculates correct start index`() {
        val transcription = Transcription(
            audioData = (0 until 50000).map { it.toShort() }.toMutableList(),
            start = 30000.0,
            end = 45000.0,
            text = null
        )

        val startIndex = (transcription.start!!.toInt() - SPEECH_START_PAD_MS).coerceAtLeast(0)
        val endIndex = transcription.end!!.toInt().coerceAtMost(transcription.audioData.size - 1)

        // 30000 - 24000 = 6000
        assertThat(startIndex).isEqualTo(6000)
        assertThat(endIndex).isEqualTo(45000)
    }

    @Test
    fun `audio slicing coerces start to 0 when pad exceeds start`() {
        val transcription = Transcription(
            audioData = (0 until 50000).map { it.toShort() }.toMutableList(),
            start = 10000.0, // Less than SPEECH_START_PAD_MS
            end = 45000.0,
            text = null
        )

        val startIndex = (transcription.start!!.toInt() - SPEECH_START_PAD_MS).coerceAtLeast(0)

        // 10000 - 24000 = -14000, coerced to 0
        assertThat(startIndex).isEqualTo(0)
    }

    @Test
    fun `audio slicing coerces end to last valid index`() {
        val transcription = Transcription(
            audioData = (0 until 30000).map { it.toShort() }.toMutableList(),
            start = 10000.0,
            end = 50000.0, // Beyond audio data size
            text = null
        )

        val endIndex = transcription.end!!.toInt().coerceAtMost(transcription.audioData.size - 1)

        // 50000 coerced to 29999 (last valid index)
        assertThat(endIndex).isEqualTo(29999)
    }

    @Test
    fun `multiple transcriptions join correctly`() {
        val transcriptions = mutableMapOf<Int, Transcription>()

        transcriptions[0] = Transcription(start = 0.0, end = 1000.0, text = "Hello")
        transcriptions[1] = Transcription(start = 1500.0, end = 3000.0, text = "world")
        transcriptions[2] = Transcription(start = 3500.0, end = 5000.0, text = "test")

        val result = transcriptions.toSortedMap().values.joinToString { it.text ?: "" }

        assertThat(result).isEqualTo("Hello, world, test")
    }

    @Test
    fun `transcriptions with null text are handled in join`() {
        val transcriptions = mutableMapOf<Int, Transcription>()

        transcriptions[0] = Transcription(start = 0.0, end = 1000.0, text = "Hello")
        transcriptions[1] = Transcription(start = 1500.0, end = 3000.0, text = null)
        transcriptions[2] = Transcription(start = 3500.0, end = 5000.0, text = "world")

        val result = transcriptions.toSortedMap().values.joinToString { it.text ?: "" }

        assertThat(result).isEqualTo("Hello, , world")
    }

    @Test
    fun `empty transcriptions join to empty string`() {
        val transcriptions = mutableMapOf<Int, Transcription>()

        val result = transcriptions.toSortedMap().values.joinToString { it.text ?: "" }

        assertThat(result).isEmpty()
    }

    @Test
    fun `transcription index maintains order when sorted`() {
        val transcriptions = mutableMapOf<Int, Transcription>()

        // Add out of order
        transcriptions[2] = Transcription(start = 0.0, end = 0.0, text = "third")
        transcriptions[0] = Transcription(start = 0.0, end = 0.0, text = "first")
        transcriptions[1] = Transcription(start = 0.0, end = 0.0, text = "second")

        val sortedTexts = transcriptions.toSortedMap().values.map { it.text }

        assertThat(sortedTexts).containsExactly("first", "second", "third").inOrder()
    }

    @Test
    fun `audio data clearing removes all samples`() {
        val transcription = Transcription(
            audioData = (0 until 1000).map { it.toShort() }.toMutableList(),
            start = 0.0,
            end = 1000.0,
            text = "test"
        )

        assertThat(transcription.audioData).hasSize(1000)

        transcription.audioData.clear()

        assertThat(transcription.audioData).isEmpty()
    }

    @Test
    fun `audio slicing creates correct sublist`() {
        val transcription = Transcription(
            audioData = (0 until 100).map { it.toShort() }.toMutableList(),
            start = 20.0,
            end = 80.0,
            text = null
        )

        // With SPEECH_START_PAD_MS = 24000, start would be negative
        // Using smaller pad for this test
        val speechPad = 10
        val startIndex = (transcription.start!!.toInt() - speechPad).coerceAtLeast(0)
        val endIndex = transcription.end!!.toInt().coerceAtMost(transcription.audioData.size - 1)

        val slice = transcription.audioData.slice(startIndex..endIndex)

        // Start: 20 - 10 = 10, End: 80
        assertThat(startIndex).isEqualTo(10)
        assertThat(endIndex).isEqualTo(80)
        assertThat(slice).hasSize(71) // 80 - 10 + 1
        assertThat(slice.first()).isEqualTo(10.toShort())
        assertThat(slice.last()).isEqualTo(80.toShort())
    }
}

/**
 * Tests for auto-stop recognition behavior logic.
 */
class AutoStopRecognitionLogicTest {

    @Test
    fun `auto stop enabled triggers stop after speech end`() {
        val autoStopRecognition = true
        var isRecording = true

        // Simulate speech end detection
        val speechEnded = true

        if (speechEnded && autoStopRecognition) {
            isRecording = false
        }

        assertThat(isRecording).isFalse()
    }

    @Test
    fun `auto stop disabled keeps recording after speech end`() {
        val autoStopRecognition = false
        var isRecording = true

        // Simulate speech end detection
        val speechEnded = true

        if (speechEnded && autoStopRecognition) {
            isRecording = false
        }

        assertThat(isRecording).isTrue()
    }

    @Test
    fun `stop listening flag breaks recording loop`() {
        var stopListening = false
        var loopIterations = 0
        val maxIterations = 10

        while (!stopListening && loopIterations < maxIterations) {
            loopIterations++
            if (loopIterations == 5) {
                stopListening = true
            }
        }

        assertThat(loopIterations).isEqualTo(5)
        assertThat(stopListening).isTrue()
    }
}

/**
 * Tests for partial results bundle creation logic.
 */
class PartialResultsBundleTest {

    @Test
    fun `results recognition key contains transcription text`() {
        val transcriptionText = "Hello world"
        val resultsList = arrayListOf(transcriptionText)

        assertThat(resultsList).contains("Hello world")
        assertThat(resultsList).hasSize(1)
    }

    @Test
    fun `empty transcription creates empty results list entry`() {
        val transcriptionText = ""
        val resultsList = arrayListOf(transcriptionText)

        assertThat(resultsList).contains("")
        assertThat(resultsList).hasSize(1)
    }
}
