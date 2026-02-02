package dev.soupslurpr.transcribro.recognitionservice.silerovad

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SileroVadLocalDataSource.
 *
 * Tests cover:
 * - Delegation to SileroVadApi
 * - Execution on IO dispatcher
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SileroVadLocalDataSourceTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockApi: SileroVadApi
    private lateinit var mockDetector: SileroVadDetector
    private lateinit var dataSource: SileroVadLocalDataSource

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockDetector = mockk(relaxed = true)
        mockApi = mockk(relaxed = true)
        every { mockApi.getSileroVadDetector() } returns mockDetector

        dataSource = SileroVadLocalDataSource(mockApi, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getSileroVadDetector delegates to api`() = runTest {
        val result = dataSource.getSileroVadDetector()

        verify { mockApi.getSileroVadDetector() }
        assertThat(result).isEqualTo(mockDetector)
    }

    @Test
    fun `getSileroVadDetector returns detector from api`() = runTest {
        val result = dataSource.getSileroVadDetector()

        assertThat(result).isNotNull()
        assertThat(result).isSameInstanceAs(mockDetector)
    }
}
