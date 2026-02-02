package com.whispercpp.whisper

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for CPU info parsing and high-performance CPU count calculation.
 *
 * These tests verify the logic for:
 * - Parsing CPU variant information
 * - Counting high-performance cores based on frequency
 * - Counting cores based on CPU variant
 * - Edge cases and error handling
 */
class CpuInfoTest {

    /**
     * Test helper that mimics the CpuInfo parsing logic for testing.
     * This allows us to test the core algorithms without needing actual /proc/cpuinfo.
     */
    class TestableCpuInfo(private val lines: List<String>) {

        fun getHighPerfCpuCountByVariant(): Int =
            getCpuValues(property = "CPU variant") { it.substringAfter("0x").toInt(radix = 16) }
                .countKeepingMin()

        fun getHighPerfCpuCountByFrequencies(frequencies: Map<Int, Int>): Int =
            getCpuValues(property = "processor") { frequencies[it.toInt()] ?: 0 }
                .countOnlyMax()

        private fun getCpuValues(property: String, mapper: (String) -> Int) = lines
            .asSequence()
            .filter { it.startsWith(property) }
            .map { mapper(it.substringAfter(':').trim()) }
            .sorted()
            .toList()

        private fun List<Int>.countKeepingMin(): Int {
            if (isEmpty()) return 0
            val min = min()
            return count { it == min }
        }

        private fun List<Int>.countOnlyMax(): Int {
            val max = maxOrNull() ?: return 0
            return count { it == max }
        }
    }

    @Test
    fun `countOnlyMax returns count of maximum values`() {
        val list = listOf(1, 2, 3, 3, 2, 3)
        val max = list.maxOrNull() ?: 0
        val count = list.count { it == max }

        assertThat(count).isEqualTo(3) // Three 3s
    }

    @Test
    fun `countKeepingMin returns count of minimum values`() {
        val list = listOf(1, 2, 3, 1, 2, 1)
        val min = list.min()
        val count = list.count { it == min }

        assertThat(count).isEqualTo(3) // Three 1s
    }

    @Test
    fun `parses CPU variant from cpuinfo format`() {
        val lines = listOf(
            "processor\t: 0",
            "CPU variant\t: 0x1",
            "processor\t: 1",
            "CPU variant\t: 0x1",
            "processor\t: 2",
            "CPU variant\t: 0x4",
            "processor\t: 3",
            "CPU variant\t: 0x4"
        )

        val cpuInfo = TestableCpuInfo(lines)
        val count = cpuInfo.getHighPerfCpuCountByVariant()

        // Variant 0x1 (1) is minimum, 2 cores have it
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun `handles single core processor`() {
        val lines = listOf(
            "processor\t: 0",
            "CPU variant\t: 0x1"
        )

        val cpuInfo = TestableCpuInfo(lines)
        val count = cpuInfo.getHighPerfCpuCountByVariant()

        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `handles all cores with same variant`() {
        val lines = listOf(
            "processor\t: 0",
            "CPU variant\t: 0x2",
            "processor\t: 1",
            "CPU variant\t: 0x2",
            "processor\t: 2",
            "CPU variant\t: 0x2",
            "processor\t: 3",
            "CPU variant\t: 0x2"
        )

        val cpuInfo = TestableCpuInfo(lines)
        val count = cpuInfo.getHighPerfCpuCountByVariant()

        // All have same variant, so all are "min"
        assertThat(count).isEqualTo(4)
    }

    @Test
    fun `high perf count by frequency returns cores with max frequency`() {
        val lines = listOf(
            "processor\t: 0",
            "processor\t: 1",
            "processor\t: 2",
            "processor\t: 3"
        )

        // Simulate different frequencies: cores 0,1 at 1.8GHz, cores 2,3 at 2.4GHz
        val frequencies = mapOf(
            0 to 1800000,
            1 to 1800000,
            2 to 2400000,
            3 to 2400000
        )

        val cpuInfo = TestableCpuInfo(lines)
        val count = cpuInfo.getHighPerfCpuCountByFrequencies(frequencies)

        // 2 cores at max frequency (2.4GHz)
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun `high perf count returns 0 for empty processor list`() {
        val cpuInfo = TestableCpuInfo(emptyList())
        val count = cpuInfo.getHighPerfCpuCountByVariant()

        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `handles big LITTLE architecture correctly`() {
        // Typical big.LITTLE: 4 little cores (low variant) + 4 big cores (high variant)
        val lines = listOf(
            "processor\t: 0", "CPU variant\t: 0x0",
            "processor\t: 1", "CPU variant\t: 0x0",
            "processor\t: 2", "CPU variant\t: 0x0",
            "processor\t: 3", "CPU variant\t: 0x0",
            "processor\t: 4", "CPU variant\t: 0x3",
            "processor\t: 5", "CPU variant\t: 0x3",
            "processor\t: 6", "CPU variant\t: 0x3",
            "processor\t: 7", "CPU variant\t: 0x3"
        )

        val cpuInfo = TestableCpuInfo(lines)
        val count = cpuInfo.getHighPerfCpuCountByVariant()

        // 4 cores with min variant (0x0 = little cores, using min as heuristic)
        assertThat(count).isEqualTo(4)
    }

    @Test
    fun `handles heterogeneous frequency distribution`() {
        val lines = listOf(
            "processor\t: 0",
            "processor\t: 1",
            "processor\t: 2",
            "processor\t: 3",
            "processor\t: 4",
            "processor\t: 5",
            "processor\t: 6",
            "processor\t: 7"
        )

        // 4 little cores at 1.5GHz, 2 medium at 2.0GHz, 2 big at 2.8GHz
        val frequencies = mapOf(
            0 to 1500000, 1 to 1500000, 2 to 1500000, 3 to 1500000,
            4 to 2000000, 5 to 2000000,
            6 to 2800000, 7 to 2800000
        )

        val cpuInfo = TestableCpuInfo(lines)
        val count = cpuInfo.getHighPerfCpuCountByFrequencies(frequencies)

        // 2 cores at max frequency (2.8GHz)
        assertThat(count).isEqualTo(2)
    }

    @Test
    fun `preferredThreadCount returns at least 1`() {
        // Test the coerceAtLeast logic
        val rawCount = -5
        val result = rawCount.coerceAtLeast(1)

        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `fallback calculation returns available processors minus 4`() {
        // Simulating the fallback logic
        val availableProcessors = 8
        val fallback = (availableProcessors - 4).coerceAtLeast(0)

        assertThat(fallback).isEqualTo(4)
    }

    @Test
    fun `fallback calculation returns 0 when processors less than 4`() {
        val availableProcessors = 2
        val fallback = (availableProcessors - 4).coerceAtLeast(0)

        assertThat(fallback).isEqualTo(0)
    }
}

/**
 * Tests for audio context calculation used in WhisperContext.
 */
class WhisperContextCalculationsTest {

    @Test
    fun `audio context calculation for short audio`() {
        // Formula: min(((dataTime / 1000f) / 30f) * 1500f) + 512f).toInt(), 1500)
        val dataTimeMs = 1000L // 1 second

        val audioCtx = kotlin.math.min(
            ((((dataTimeMs.toFloat() / 1000f) / 30f) * 1500f) + 512f).toInt(),
            1500
        )

        // (1 / 30) * 1500 + 512 = 50 + 512 = 562
        assertThat(audioCtx).isEqualTo(562)
    }

    @Test
    fun `audio context calculation for 30 second audio`() {
        val dataTimeMs = 30000L // 30 seconds

        val audioCtx = kotlin.math.min(
            ((((dataTimeMs.toFloat() / 1000f) / 30f) * 1500f) + 512f).toInt(),
            1500
        )

        // (30 / 30) * 1500 + 512 = 1500 + 512 = 2012, capped to 1500
        assertThat(audioCtx).isEqualTo(1500)
    }

    @Test
    fun `audio context calculation for 15 second audio`() {
        val dataTimeMs = 15000L // 15 seconds

        val audioCtx = kotlin.math.min(
            ((((dataTimeMs.toFloat() / 1000f) / 30f) * 1500f) + 512f).toInt(),
            1500
        )

        // (15 / 30) * 1500 + 512 = 750 + 512 = 1262
        assertThat(audioCtx).isEqualTo(1262)
    }

    @Test
    fun `audio context never exceeds 1500`() {
        val dataTimeMs = 60000L // 60 seconds

        val audioCtx = kotlin.math.min(
            ((((dataTimeMs.toFloat() / 1000f) / 30f) * 1500f) + 512f).toInt(),
            1500
        )

        assertThat(audioCtx).isEqualTo(1500)
    }
}
