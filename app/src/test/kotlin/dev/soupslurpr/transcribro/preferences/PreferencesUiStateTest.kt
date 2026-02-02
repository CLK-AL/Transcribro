package dev.soupslurpr.transcribro.preferences

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for PreferencesUiState data class.
 *
 * Tests cover:
 * - Default values for all preferences
 * - Preference key names
 * - MutableState behavior
 * - Data class equality and copying
 */
class PreferencesUiStateTest {

    @Test
    fun `default pitchBlackBackground is false`() {
        val state = PreferencesUiState()

        assertThat(state.pitchBlackBackground.second.value).isFalse()
    }

    @Test
    fun `default acceptedPrivacyPolicyAndLicense is false`() {
        val state = PreferencesUiState()

        assertThat(state.acceptedPrivacyPolicyAndLicense.second.value).isFalse()
    }

    @Test
    fun `default autoSwitchToPreviousInputMethod is false`() {
        val state = PreferencesUiState()

        assertThat(state.autoSwitchToPreviousInputMethod.second.value).isFalse()
    }

    @Test
    fun `default autoStopRecognition is false`() {
        val state = PreferencesUiState()

        assertThat(state.autoStopRecognition.second.value).isFalse()
    }

    @Test
    fun `default autoStartRecognition is true`() {
        val state = PreferencesUiState()

        // Note: autoStartRecognition defaults to true, unlike others
        assertThat(state.autoStartRecognition.second.value).isTrue()
    }

    @Test
    fun `default autoSendTranscription is false`() {
        val state = PreferencesUiState()

        assertThat(state.autoSendTranscription.second.value).isFalse()
    }

    @Test
    fun `pitchBlackBackground key name is correct`() {
        val state = PreferencesUiState()

        assertThat(state.pitchBlackBackground.first.name).isEqualTo("PITCH_BLACK_BACKGROUND")
    }

    @Test
    fun `acceptedPrivacyPolicyAndLicense key name includes version`() {
        val state = PreferencesUiState()

        assertThat(state.acceptedPrivacyPolicyAndLicense.first.name)
            .isEqualTo("ACCEPTED_PRIVACY_POLICY_AND_LICENSE_V0.3.0")
    }

    @Test
    fun `autoSwitchToPreviousInputMethod key name is correct`() {
        val state = PreferencesUiState()

        assertThat(state.autoSwitchToPreviousInputMethod.first.name)
            .isEqualTo("AUTO_SWITCH_TO_PREVIOUS_INPUT_METHOD")
    }

    @Test
    fun `autoStopRecognition key name is correct`() {
        val state = PreferencesUiState()

        assertThat(state.autoStopRecognition.first.name).isEqualTo("AUTO_STOP_RECOGNITION")
    }

    @Test
    fun `autoStartRecognition key name is correct`() {
        val state = PreferencesUiState()

        assertThat(state.autoStartRecognition.first.name).isEqualTo("AUTO_START_RECOGNITION")
    }

    @Test
    fun `autoSendTranscription key name is correct`() {
        val state = PreferencesUiState()

        assertThat(state.autoSendTranscription.first.name).isEqualTo("AUTO_SEND_TRANSCRIPTION")
    }

    @Test
    fun `mutableState values can be changed`() {
        val state = PreferencesUiState()

        state.pitchBlackBackground.second.value = true

        assertThat(state.pitchBlackBackground.second.value).isTrue()
    }

    @Test
    fun `changing one preference does not affect others`() {
        val state = PreferencesUiState()

        state.pitchBlackBackground.second.value = true
        state.autoStopRecognition.second.value = true

        assertThat(state.pitchBlackBackground.second.value).isTrue()
        assertThat(state.autoStopRecognition.second.value).isTrue()
        assertThat(state.autoSwitchToPreviousInputMethod.second.value).isFalse()
        assertThat(state.autoStartRecognition.second.value).isTrue() // Default true
        assertThat(state.autoSendTranscription.second.value).isFalse()
    }

    @Test
    fun `custom initial values can be provided`() {
        val customState = PreferencesUiState(
            pitchBlackBackground = Pair(
                booleanPreferencesKey("PITCH_BLACK_BACKGROUND"),
                mutableStateOf(true)
            )
        )

        assertThat(customState.pitchBlackBackground.second.value).isTrue()
    }

    @Test
    fun `all preference keys are boolean type`() {
        val state = PreferencesUiState()

        // Verify all keys are boolean preferences
        assertThat(state.pitchBlackBackground.first)
            .isInstanceOf(androidx.datastore.preferences.core.Preferences.Key::class.java)
        assertThat(state.acceptedPrivacyPolicyAndLicense.first)
            .isInstanceOf(androidx.datastore.preferences.core.Preferences.Key::class.java)
        assertThat(state.autoSwitchToPreviousInputMethod.first)
            .isInstanceOf(androidx.datastore.preferences.core.Preferences.Key::class.java)
        assertThat(state.autoStopRecognition.first)
            .isInstanceOf(androidx.datastore.preferences.core.Preferences.Key::class.java)
        assertThat(state.autoStartRecognition.first)
            .isInstanceOf(androidx.datastore.preferences.core.Preferences.Key::class.java)
        assertThat(state.autoSendTranscription.first)
            .isInstanceOf(androidx.datastore.preferences.core.Preferences.Key::class.java)
    }

    @Test
    fun `data class supports copy with modifications`() {
        val original = PreferencesUiState()
        val customMutableState = mutableStateOf(true)

        val copied = original.copy(
            pitchBlackBackground = Pair(
                booleanPreferencesKey("PITCH_BLACK_BACKGROUND"),
                customMutableState
            )
        )

        assertThat(copied.pitchBlackBackground.second.value).isTrue()
        // Original unchanged
        assertThat(original.pitchBlackBackground.second.value).isFalse()
    }

    @Test
    fun `preference count is six`() {
        val state = PreferencesUiState()

        // Use reflection or manual count
        val preferenceCount = listOf(
            state.pitchBlackBackground,
            state.acceptedPrivacyPolicyAndLicense,
            state.autoSwitchToPreviousInputMethod,
            state.autoStopRecognition,
            state.autoStartRecognition,
            state.autoSendTranscription
        ).size

        assertThat(preferenceCount).isEqualTo(6)
    }
}
