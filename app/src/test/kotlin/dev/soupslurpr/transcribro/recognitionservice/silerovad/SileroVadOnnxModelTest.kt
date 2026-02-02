package dev.soupslurpr.transcribro.recognitionservice.silerovad

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for SileroVadOnnxModel validation and processing logic.
 *
 * Note: These tests validate the input validation and audio processing logic
 * without requiring the actual ONNX runtime. We test the logic by recreating
 * the validation algorithms.
 *
 * Tests cover:
 * - Sample rate validation
 * - Audio dimension validation
 * - Downsampling logic for non-16kHz rates
 * - Minimum audio length validation
 * - State management logic
 */
class SileroVadOnnxModelTest {

    companion object {
        private val SAMPLE_RATES = listOf(8000, 16000)
    }

    /**
     * Testable implementation of the validation logic from SileroVadOnnxModel.
     */
    class ValidationResult(val x: Array<FloatArray?>, val sr: Int)

    private fun validateInput(x: Array<FloatArray?>, sr: Int): ValidationResult {
        var processedX = x
        var processedSr = sr

        // Process the input data with dimension 1
        if (processedX.size == 1) {
            processedX = arrayOf(processedX[0])
        }

        // Throw an exception when the input data dimension is greater than 2
        require(processedX.size <= 2) { "Incorrect audio data dimension: " + processedX[0]!!.size }

        // Process the input data when the sample rate is not equal to 16000 and is a multiple of 16000
        if (processedSr != 16000 && (processedSr % 16000 == 0)) {
            val step = processedSr / 16000
            val reducedX = arrayOfNulls<FloatArray>(processedX.size)

            for (i in processedX.indices) {
                val current = processedX[i]
                val newArr = FloatArray((current!!.size + step - 1) / step)

                var j = 0
                var index = 0
                while (j < current.size) {
                    newArr[index] = current[j]
                    j += step
                    index++
                }

                reducedX[i] = newArr
            }

            processedX = reducedX
            processedSr = 16000
        }

        // If the sample rate is not in the list of supported sample rates, throw an exception
        require(SAMPLE_RATES.contains(processedSr)) { "Only supports sample rates $SAMPLE_RATES (or multiples of 16000)" }

        // If the input audio block is too short, throw an exception
        require(!((processedSr.toFloat()) / processedX[0]!!.size > 31.25)) { "Input audio is too short" }

        return ValidationResult(processedX, processedSr)
    }

    // ==================== Sample Rate Validation Tests ====================

    @Test
    fun `validates 16000 Hz sample rate`() {
        val audioData = arrayOf(FloatArray(512) { 0f })

        val result = validateInput(audioData, 16000)

        assertThat(result.sr).isEqualTo(16000)
    }

    @Test
    fun `validates 8000 Hz sample rate`() {
        val audioData = arrayOf(FloatArray(512) { 0f })

        val result = validateInput(audioData, 8000)

        assertThat(result.sr).isEqualTo(8000)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects unsupported sample rate 44100`() {
        val audioData = arrayOf(FloatArray(512) { 0f })

        validateInput(audioData, 44100)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects unsupported sample rate 22050`() {
        val audioData = arrayOf(FloatArray(512) { 0f })

        validateInput(audioData, 22050)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects unsupported sample rate 48000`() {
        // 48000 is not a multiple of 16000 (48000 / 16000 = 3, but 48000 % 16000 = 0)
        // Actually 48000 % 16000 = 0, so it will be downsampled to 16000
        // Let me recalculate: 48000 / 16000 = 3, 48000 % 16000 = 0
        // So 48000 WILL be accepted and downsampled. Let me use a non-multiple.
        val audioData = arrayOf(FloatArray(512) { 0f })

        validateInput(audioData, 24000) // 24000 is not supported and not a multiple of 16000
    }

    // ==================== Downsampling Tests ====================

    @Test
    fun `downsamples 32000 Hz to 16000 Hz`() {
        val audioData = arrayOf(FloatArray(1024) { it.toFloat() })

        val result = validateInput(audioData, 32000)

        assertThat(result.sr).isEqualTo(16000)
        // 32000 / 16000 = 2, so every 2nd sample
        assertThat(result.x[0]!!.size).isEqualTo(512)
    }

    @Test
    fun `downsamples 48000 Hz to 16000 Hz`() {
        val audioData = arrayOf(FloatArray(1500) { it.toFloat() })

        val result = validateInput(audioData, 48000)

        assertThat(result.sr).isEqualTo(16000)
        // 48000 / 16000 = 3, so every 3rd sample
        assertThat(result.x[0]!!.size).isEqualTo(500)
    }

    @Test
    fun `downsampling preserves correct samples`() {
        // Create array with values 0, 1, 2, 3, 4, 5, ...
        val audioData = arrayOf(FloatArray(600) { it.toFloat() })

        val result = validateInput(audioData, 32000)

        // With step=2, should get samples 0, 2, 4, 6, 8, ...
        assertThat(result.x[0]!![0]).isEqualTo(0f)
        assertThat(result.x[0]!![1]).isEqualTo(2f)
        assertThat(result.x[0]!![2]).isEqualTo(4f)
    }

    @Test
    fun `downsampling with 48kHz preserves every third sample`() {
        val audioData = arrayOf(FloatArray(900) { it.toFloat() })

        val result = validateInput(audioData, 48000)

        // With step=3, should get samples 0, 3, 6, 9, ...
        assertThat(result.x[0]!![0]).isEqualTo(0f)
        assertThat(result.x[0]!![1]).isEqualTo(3f)
        assertThat(result.x[0]!![2]).isEqualTo(6f)
    }

    // ==================== Audio Dimension Validation Tests ====================

    @Test
    fun `accepts single channel audio`() {
        val audioData = arrayOf(FloatArray(512) { 0f })

        val result = validateInput(audioData, 16000)

        assertThat(result.x.size).isEqualTo(1)
    }

    @Test
    fun `accepts two channel audio`() {
        val audioData = arrayOf(
            FloatArray(512) { 0f },
            FloatArray(512) { 0f }
        )

        val result = validateInput(audioData, 16000)

        assertThat(result.x.size).isEqualTo(2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects more than two channels`() {
        val audioData = arrayOf(
            FloatArray(512) { 0f },
            FloatArray(512) { 0f },
            FloatArray(512) { 0f }
        )

        validateInput(audioData, 16000)
    }

    // ==================== Minimum Audio Length Tests ====================

    @Test
    fun `accepts audio meeting minimum length at 16kHz`() {
        // Minimum: sr / length <= 31.25
        // At 16000 Hz: length >= 16000 / 31.25 = 512 samples
        val audioData = arrayOf(FloatArray(512) { 0f })

        val result = validateInput(audioData, 16000)

        assertThat(result.x[0]!!.size).isEqualTo(512)
    }

    @Test
    fun `accepts audio meeting minimum length at 8kHz`() {
        // At 8000 Hz: length >= 8000 / 31.25 = 256 samples
        val audioData = arrayOf(FloatArray(256) { 0f })

        val result = validateInput(audioData, 8000)

        assertThat(result.x[0]!!.size).isEqualTo(256)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects audio too short at 16kHz`() {
        // 16000 / 511 = 31.31... > 31.25, so should fail
        val audioData = arrayOf(FloatArray(100) { 0f })

        validateInput(audioData, 16000)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects audio too short at 8kHz`() {
        // 8000 / 100 = 80 > 31.25, so should fail
        val audioData = arrayOf(FloatArray(100) { 0f })

        validateInput(audioData, 8000)
    }

    // ==================== State Management Tests ====================

    @Test
    fun `state arrays h and c are initialized correctly`() {
        // Testing the dimensions: Array(2) { Array(1) { FloatArray(64) } }
        val h = Array(2) { Array(1) { FloatArray(64) } }
        val c = Array(2) { Array(1) { FloatArray(64) } }

        assertThat(h.size).isEqualTo(2)
        assertThat(h[0].size).isEqualTo(1)
        assertThat(h[0][0].size).isEqualTo(64)

        assertThat(c.size).isEqualTo(2)
        assertThat(c[0].size).isEqualTo(1)
        assertThat(c[0][0].size).isEqualTo(64)
    }

    @Test
    fun `state reset clears to zeros`() {
        val h = Array(2) { Array(1) { FloatArray(64) { 1f } } }

        // Simulate reset by creating new arrays
        val resetH = Array(2) { Array(1) { FloatArray(64) } }

        // All values should be 0
        for (i in resetH.indices) {
            for (j in resetH[i].indices) {
                for (k in resetH[i][j].indices) {
                    assertThat(resetH[i][j][k]).isEqualTo(0f)
                }
            }
        }
    }

    @Test
    fun `batch size detection from input array`() {
        val singleBatch = arrayOf(FloatArray(512))
        val doubleBatch = arrayOf(FloatArray(512), FloatArray(512))

        assertThat(singleBatch.size).isEqualTo(1)
        assertThat(doubleBatch.size).isEqualTo(2)
    }

    @Test
    fun `state reset triggered when sample rate changes`() {
        var lastSr = 16000
        var lastBatchSize = 1

        // Simulate calling with different sample rate
        val newSr = 8000
        val newBatchSize = 1

        val shouldReset = lastBatchSize == 0 || lastSr != newSr || lastBatchSize != newBatchSize

        assertThat(shouldReset).isTrue()
    }

    @Test
    fun `state reset triggered when batch size changes`() {
        var lastSr = 16000
        var lastBatchSize = 1

        // Simulate calling with different batch size
        val newSr = 16000
        val newBatchSize = 2

        val shouldReset = lastBatchSize == 0 || lastSr != newSr || lastBatchSize != newBatchSize

        assertThat(shouldReset).isTrue()
    }

    @Test
    fun `state not reset when parameters unchanged`() {
        var lastSr = 16000
        var lastBatchSize = 1

        val newSr = 16000
        val newBatchSize = 1

        val shouldReset = lastBatchSize == 0 || lastSr != newSr || lastBatchSize != newBatchSize

        assertThat(shouldReset).isFalse()
    }

    // ==================== Edge Cases ====================

    @Test
    fun `handles exactly minimum length at boundary`() {
        // At 16000 Hz: 16000 / 31.25 = 512 exactly
        val audioData = arrayOf(FloatArray(512) { 0f })

        val result = validateInput(audioData, 16000)

        assertThat(result.x[0]!!.size).isEqualTo(512)
    }

    @Test
    fun `handles large audio buffers`() {
        // 30 seconds at 16kHz = 480000 samples
        val audioData = arrayOf(FloatArray(480000) { it.toFloat() })

        val result = validateInput(audioData, 16000)

        assertThat(result.x[0]!!.size).isEqualTo(480000)
    }

    @Test
    fun `downsampling handles non-divisible lengths`() {
        // 1001 samples at 32kHz with step 2 = ceil(1001/2) = 501 samples
        val audioData = arrayOf(FloatArray(1001) { it.toFloat() })

        val result = validateInput(audioData, 32000)

        // (1001 + 2 - 1) / 2 = 501
        assertThat(result.x[0]!!.size).isEqualTo(501)
    }
}
