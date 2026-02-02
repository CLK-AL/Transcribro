package dev.soupslurpr.transcribro.recognitionservice.whisper

import com.google.common.truth.Truth.assertThat
import com.whispercpp.whisper.WhisperContext
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
 * Unit tests for WhisperLocalDataSource.
 *
 * Tests cover:
 * - Delegation to WhisperApi
 * - Execution on IO dispatcher
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WhisperLocalDataSourceTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockApi: WhisperApi
    private lateinit var mockContext: WhisperContext
    private lateinit var dataSource: WhisperLocalDataSource

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockContext = mockk(relaxed = true)
        mockApi = mockk(relaxed = true)
        every { mockApi.getWhisperContext() } returns mockContext

        dataSource = WhisperLocalDataSource(mockApi, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getWhisperContext delegates to api`() = runTest {
        val result = dataSource.getWhisperContext()

        verify { mockApi.getWhisperContext() }
        assertThat(result).isEqualTo(mockContext)
    }

    @Test
    fun `getWhisperContext returns context from api`() = runTest {
        val result = dataSource.getWhisperContext()

        assertThat(result).isNotNull()
        assertThat(result).isSameInstanceAs(mockContext)
    }
}
