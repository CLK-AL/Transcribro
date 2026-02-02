package dev.soupslurpr.transcribro.preferences

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for PreferencesViewModel.
 *
 * Tests cover:
 * - Initial state with default preference values
 * - Loading preferences from DataStore
 * - Setting preferences and persisting to DataStore
 * - State flow updates
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockDataStore: DataStore<Preferences>
    private lateinit var preferencesFlow: MutableStateFlow<Preferences>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockDataStore = mockk(relaxed = true)
        preferencesFlow = MutableStateFlow(preferencesOf())
        every { mockDataStore.data } returns preferencesFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val viewModel = PreferencesViewModel(mockDataStore)

        assertThat(viewModel.uiState.value.pitchBlackBackground.second.value).isFalse()
        assertThat(viewModel.uiState.value.acceptedPrivacyPolicyAndLicense.second.value).isFalse()
        assertThat(viewModel.uiState.value.autoSwitchToPreviousInputMethod.second.value).isFalse()
        assertThat(viewModel.uiState.value.autoStopRecognition.second.value).isFalse()
        assertThat(viewModel.uiState.value.autoStartRecognition.second.value).isTrue() // Default is true
        assertThat(viewModel.uiState.value.autoSendTranscription.second.value).isFalse()
    }

    @Test
    fun `loads preferences from DataStore on init`() = runTest {
        val prefs = preferencesOf(
            booleanPreferencesKey("PITCH_BLACK_BACKGROUND") to true,
            booleanPreferencesKey("AUTO_STOP_RECOGNITION") to true
        )
        preferencesFlow.value = prefs

        val viewModel = PreferencesViewModel(mockDataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.pitchBlackBackground.second.value).isTrue()
        assertThat(viewModel.uiState.value.autoStopRecognition.second.value).isTrue()
    }

    @Test
    fun `setPreference updates DataStore`() = runTest {
        val transformSlot = slot<suspend (MutablePreferences) -> Unit>()
        coEvery { mockDataStore.edit(capture(transformSlot)) } coAnswers {
            val mutablePrefs = mockk<MutablePreferences>(relaxed = true)
            transformSlot.captured.invoke(mutablePrefs)
            preferencesOf()
        }

        val viewModel = PreferencesViewModel(mockDataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        val key = booleanPreferencesKey("PITCH_BLACK_BACKGROUND")
        viewModel.setPreference(key, true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockDataStore.edit(any()) }
    }

    @Test
    fun `preference keys are correctly defined`() = runTest {
        val viewModel = PreferencesViewModel(mockDataStore)

        // Verify all preference keys are correctly named
        assertThat(viewModel.uiState.value.pitchBlackBackground.first.name)
            .isEqualTo("PITCH_BLACK_BACKGROUND")
        assertThat(viewModel.uiState.value.acceptedPrivacyPolicyAndLicense.first.name)
            .isEqualTo("ACCEPTED_PRIVACY_POLICY_AND_LICENSE_V0.3.0")
        assertThat(viewModel.uiState.value.autoSwitchToPreviousInputMethod.first.name)
            .isEqualTo("AUTO_SWITCH_TO_PREVIOUS_INPUT_METHOD")
        assertThat(viewModel.uiState.value.autoStopRecognition.first.name)
            .isEqualTo("AUTO_STOP_RECOGNITION")
        assertThat(viewModel.uiState.value.autoStartRecognition.first.name)
            .isEqualTo("AUTO_START_RECOGNITION")
        assertThat(viewModel.uiState.value.autoSendTranscription.first.name)
            .isEqualTo("AUTO_SEND_TRANSCRIPTION")
    }

    @Test
    fun `uiState is exposed as StateFlow`() = runTest {
        val viewModel = PreferencesViewModel(mockDataStore)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(PreferencesUiState::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updates reflect when DataStore emits new values`() = runTest {
        val viewModel = PreferencesViewModel(mockDataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        // Emit new preferences
        preferencesFlow.value = preferencesOf(
            booleanPreferencesKey("PITCH_BLACK_BACKGROUND") to true
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.pitchBlackBackground.second.value).isTrue()
    }

    @Test
    fun `factory creates ViewModel correctly`() = runTest {
        val factory = PreferencesViewModel.PreferencesViewModelFactory(mockDataStore)

        val viewModel = factory.create(PreferencesViewModel::class.java)

        assertThat(viewModel).isInstanceOf(PreferencesViewModel::class.java)
    }

    @Test
    fun `factory throws for unknown ViewModel class`() = runTest {
        val factory = PreferencesViewModel.PreferencesViewModelFactory(mockDataStore)

        try {
            factory.create(TestViewModel::class.java)
            assert(false) { "Should have thrown IllegalArgumentException" }
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("Unknown ViewModel class")
        }
    }

    @Test
    fun `preserves default values when DataStore key is missing`() = runTest {
        // DataStore returns prefs with only some keys
        val prefs = preferencesOf(
            booleanPreferencesKey("PITCH_BLACK_BACKGROUND") to true
            // Other keys missing - should use defaults
        )
        preferencesFlow.value = prefs

        val viewModel = PreferencesViewModel(mockDataStore)
        testDispatcher.scheduler.advanceUntilIdle()

        // Set value should be loaded
        assertThat(viewModel.uiState.value.pitchBlackBackground.second.value).isTrue()
        // Missing values should use defaults
        assertThat(viewModel.uiState.value.autoStartRecognition.second.value).isTrue() // Default
        assertThat(viewModel.uiState.value.autoStopRecognition.second.value).isFalse() // Default
    }
}

// Dummy class for testing factory error handling
private abstract class TestViewModel : androidx.lifecycle.ViewModel()
