package dev.soupslurpr.transcribro.recognitionservice.silerovad

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SileroVadRepository.
 *
 * Tests cover:
 * - Audio data conversion from ShortArray to FloatArray
 * - Lazy loading of the VAD detector
 * - Delegation to detector for detection
 * - Reset and release lifecycle management
 */
class SileroVadRepositoryTest {

    private lateinit var mockDataSource: SileroVadLocalDataSource
    private lateinit var mockDetector: SileroVadDetector
    private lateinit var repository: SileroVadRepository

    @Before
    fun setup() {
        mockDetector = mockk(relaxed = true)
        mockDataSource = mockk(relaxed = true)

        coEvery { mockDataSource.getSileroVadDetector() } returns mockDetector

        repository = SileroVadRepository(mockDataSource)
    }

    @Test
    fun `detect loads detector on first call`() = runTest {
        val audioData = ShortArray(512) { 0 }
        every { mockDetector.apply(any(), any()) } returns emptyMap()

        repository.detect(audioData)

        coVerify(exactly = 1) { mockDataSource.getSileroVadDetector() }
    }

    @Test
    fun `detect does not reload detector on subsequent calls`() = runTest {
        val audioData = ShortArray(512) { 0 }
        every { mockDetector.apply(any(), any()) } returns emptyMap()

        repository.detect(audioData)
        repository.detect(audioData)
        repository.detect(audioData)

        coVerify(exactly = 1) { mockDataSource.getSileroVadDetector() }
    }

    @Test
    fun `detect converts ShortArray to normalized FloatArray`() = runTest {
        // Create audio data with known values
        val audioData = shortArrayOf(0, 16383, -16384, 32767, -32768)

        var capturedBuffer: FloatArray? = null
        every { mockDetector.apply(any(), any()) } answers {
            capturedBuffer = firstArg()
            emptyMap()
        }

        repository.detect(audioData)

        // Verify conversion: short / 32767.0f, coerced to [-1, 1]
        assertThat(capturedBuffer).isNotNull()
        assertThat(capturedBuffer!!.size).isEqualTo(5)
        assertThat(capturedBuffer!![0]).isWithin(0.001f).of(0f)
        assertThat(capturedBuffer!![1]).isWithin(0.001f).of(0.5f)
        assertThat(capturedBuffer!![2]).isWithin(0.001f).of(-0.5f)
        assertThat(capturedBuffer!![3]).isWithin(0.001f).of(1f)
        assertThat(capturedBuffer!![4]).isWithin(0.001f).of(-1f) // Coerced from -32768/32767
    }

    @Test
    fun `detect coerces values to range -1 to 1`() = runTest {
        // -32768 / 32767.0f = -1.00003, should be coerced to -1
        val audioData = shortArrayOf(Short.MIN_VALUE)

        var capturedBuffer: FloatArray? = null
        every { mockDetector.apply(any(), any()) } answers {
            capturedBuffer = firstArg()
            emptyMap()
        }

        repository.detect(audioData)

        assertThat(capturedBuffer!![0]).isAtLeast(-1f)
        assertThat(capturedBuffer!![0]).isAtMost(1f)
    }

    @Test
    fun `detect returns result from detector`() = runTest {
        val audioData = ShortArray(512) { 0 }
        val expectedResult = mapOf("start" to 100.0)
        every { mockDetector.apply(any(), any()) } returns expectedResult

        val result = repository.detect(audioData)

        assertThat(result).isEqualTo(expectedResult)
    }

    @Test
    fun `detect passes returnSeconds as true to detector`() = runTest {
        val audioData = ShortArray(512) { 0 }
        every { mockDetector.apply(any(), any()) } returns emptyMap()

        repository.detect(audioData)

        verify { mockDetector.apply(any(), eq(true)) }
    }

    @Test
    fun `reset calls detector reset`() = runTest {
        // First load the detector
        val audioData = ShortArray(512) { 0 }
        every { mockDetector.apply(any(), any()) } returns emptyMap()
        repository.detect(audioData)

        repository.reset()

        verify { mockDetector.reset() }
    }

    @Test
    fun `release calls detector close`() = runTest {
        // First load the detector
        val audioData = ShortArray(512) { 0 }
        every { mockDetector.apply(any(), any()) } returns emptyMap()
        repository.detect(audioData)

        repository.release()

        verify { mockDetector.close() }
    }

    @Test
    fun `detect handles empty audio data`() = runTest {
        val audioData = ShortArray(0)
        every { mockDetector.apply(any(), any()) } returns emptyMap()

        val result = repository.detect(audioData)

        assertThat(result).isEmpty()
        verify { mockDetector.apply(match { it.isEmpty() }, any()) }
    }

    @Test
    fun `detect handles typical audio buffer size`() = runTest {
        // Typical buffer size for 16kHz audio
        val bufferSize = 512
        val audioData = ShortArray(bufferSize) { (it % 1000).toShort() }
        every { mockDetector.apply(any(), any()) } returns mapOf("start" to 50.0)

        val result = repository.detect(audioData)

        verify { mockDetector.apply(match { it.size == bufferSize }, any()) }
        assertThat(result).containsKey("start")
    }
}
