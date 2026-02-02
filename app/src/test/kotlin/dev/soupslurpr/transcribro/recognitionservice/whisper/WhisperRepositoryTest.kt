package dev.soupslurpr.transcribro.recognitionservice.whisper

import com.google.common.truth.Truth.assertThat
import com.whispercpp.whisper.WhisperContext
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for WhisperRepository.
 *
 * Tests cover:
 * - Audio data conversion from ShortArray to normalized FloatArray
 * - Minimum buffer size padding (32000 samples for ~2 seconds at 16kHz)
 * - Lazy loading of Whisper context
 * - Hallucination removal (" ." suffix)
 * - Duration calculation for audio context
 */
class WhisperRepositoryTest {

    private lateinit var mockDataSource: WhisperLocalDataSource
    private lateinit var mockContext: WhisperContext
    private lateinit var repository: WhisperRepository

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockDataSource = mockk(relaxed = true)

        coEvery { mockDataSource.getWhisperContext() } returns mockContext

        repository = WhisperRepository(mockDataSource)
    }

    @Test
    fun `transcribeAudio loads context on first call`() = runTest {
        val audioData = ShortArray(512) { 0 }
        coEvery { mockContext.transcribeData(any(), any()) } returns ""

        repository.transcribeAudio(audioData)

        coVerify(exactly = 1) { mockDataSource.getWhisperContext() }
    }

    @Test
    fun `transcribeAudio does not reload context on subsequent calls`() = runTest {
        val audioData = ShortArray(512) { 0 }
        coEvery { mockContext.transcribeData(any(), any()) } returns ""

        repository.transcribeAudio(audioData)
        repository.transcribeAudio(audioData)
        repository.transcribeAudio(audioData)

        coVerify(exactly = 1) { mockDataSource.getWhisperContext() }
    }

    @Test
    fun `transcribeAudio converts ShortArray to normalized FloatArray`() = runTest {
        // Create audio data with known values
        val audioData = shortArrayOf(0, 16383, -16384, 32767, -32768)

        val capturedBuffer = slot<FloatArray>()
        coEvery { mockContext.transcribeData(capture(capturedBuffer), any()) } returns ""

        repository.transcribeAudio(audioData)

        // Verify conversion: short / 32767.0f, coerced to [-1, 1]
        val buffer = capturedBuffer.captured
        // Buffer will be padded to 32000, but first 5 values should match
        assertThat(buffer[0]).isWithin(0.001f).of(0f)
        assertThat(buffer[1]).isWithin(0.001f).of(0.5f)
        assertThat(buffer[2]).isWithin(0.001f).of(-0.5f)
        assertThat(buffer[3]).isWithin(0.001f).of(1f)
        assertThat(buffer[4]).isWithin(0.001f).of(-1f)
    }

    @Test
    fun `transcribeAudio pads buffer to 32000 samples for short audio`() = runTest {
        val audioData = ShortArray(1000) { 100 }

        val capturedBuffer = slot<FloatArray>()
        coEvery { mockContext.transcribeData(capture(capturedBuffer), any()) } returns ""

        repository.transcribeAudio(audioData)

        assertThat(capturedBuffer.captured.size).isEqualTo(32000)
    }

    @Test
    fun `transcribeAudio fills padding with zeros`() = runTest {
        val audioData = ShortArray(1000) { 100 }

        val capturedBuffer = slot<FloatArray>()
        coEvery { mockContext.transcribeData(capture(capturedBuffer), any()) } returns ""

        repository.transcribeAudio(audioData)

        // Check that padding (after original data) is zeros
        val buffer = capturedBuffer.captured
        for (i in 1000 until 32000) {
            assertThat(buffer[i]).isEqualTo(0f)
        }
    }

    @Test
    fun `transcribeAudio does not pad buffer longer than 32000 samples`() = runTest {
        val audioData = ShortArray(50000) { 100 }

        val capturedBuffer = slot<FloatArray>()
        coEvery { mockContext.transcribeData(capture(capturedBuffer), any()) } returns ""

        repository.transcribeAudio(audioData)

        assertThat(capturedBuffer.captured.size).isEqualTo(50000)
    }

    @Test
    fun `transcribeAudio calculates duration correctly`() = runTest {
        // 16000 samples = 1 second at 16kHz
        val audioData = ShortArray(16000) { 0 }

        val capturedDuration = slot<Long>()
        coEvery { mockContext.transcribeData(any(), capture(capturedDuration)) } returns ""

        repository.transcribeAudio(audioData)

        // Duration = (16000 / 16000f) * 1000 = 1000ms
        assertThat(capturedDuration.captured).isEqualTo(1000L)
    }

    @Test
    fun `transcribeAudio removes hallucination suffix`() = runTest {
        coEvery { mockContext.transcribeData(any(), any()) } returns "Hello world ."

        val result = repository.transcribeAudio(ShortArray(32000) { 0 })

        assertThat(result).isEqualTo("Hello world")
    }

    @Test
    fun `transcribeAudio does not modify text without hallucination suffix`() = runTest {
        coEvery { mockContext.transcribeData(any(), any()) } returns "Hello world"

        val result = repository.transcribeAudio(ShortArray(32000) { 0 })

        assertThat(result).isEqualTo("Hello world")
    }

    @Test
    fun `transcribeAudio handles empty transcription`() = runTest {
        coEvery { mockContext.transcribeData(any(), any()) } returns ""

        val result = repository.transcribeAudio(ShortArray(32000) { 0 })

        assertThat(result).isEmpty()
    }

    @Test
    fun `transcribeAudio handles transcription that is only hallucination`() = runTest {
        coEvery { mockContext.transcribeData(any(), any()) } returns " ."

        val result = repository.transcribeAudio(ShortArray(32000) { 0 })

        assertThat(result).isEmpty()
    }

    @Test
    fun `release calls context release`() = runTest {
        // First load the context
        coEvery { mockContext.transcribeData(any(), any()) } returns ""
        repository.transcribeAudio(ShortArray(32000) { 0 })

        repository.release()

        coVerify { mockContext.release() }
    }

    @Test
    fun `transcribeAudio coerces values to valid range`() = runTest {
        // Short.MIN_VALUE / 32767.0f slightly exceeds -1
        val audioData = shortArrayOf(Short.MIN_VALUE, Short.MAX_VALUE)

        val capturedBuffer = slot<FloatArray>()
        coEvery { mockContext.transcribeData(capture(capturedBuffer), any()) } returns ""

        repository.transcribeAudio(audioData)

        val buffer = capturedBuffer.captured
        assertThat(buffer[0]).isAtLeast(-1f)
        assertThat(buffer[0]).isAtMost(1f)
        assertThat(buffer[1]).isAtLeast(-1f)
        assertThat(buffer[1]).isAtMost(1f)
    }

    @Test
    fun `transcribeAudio calculates duration for padded buffer using original size`() = runTest {
        // 8000 samples = 0.5 seconds at 16kHz
        val audioData = ShortArray(8000) { 0 }

        val capturedDuration = slot<Long>()
        coEvery { mockContext.transcribeData(any(), capture(capturedDuration)) } returns ""

        repository.transcribeAudio(audioData)

        // Duration should be based on original size, not padded size
        // Duration = (8000 / 16000f) * 1000 = 500ms
        assertThat(capturedDuration.captured).isEqualTo(500L)
    }
}
