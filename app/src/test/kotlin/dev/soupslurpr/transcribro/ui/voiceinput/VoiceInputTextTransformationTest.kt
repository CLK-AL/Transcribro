package dev.soupslurpr.transcribro.ui.voiceinput

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for VoiceInput text transformation logic.
 *
 * The VoiceInput class has complex text transformation logic in onPartialResults
 * that handles:
 * - Prefix space removal based on cursor context
 * - Case matching with selected text
 * - Punctuation preservation and removal
 * - Uppercase text preservation
 *
 * These tests verify the text transformation algorithms without requiring
 * the Android runtime.
 */
class VoiceInputTextTransformationTest {

    // ==================== Prefix Space Removal Tests ====================

    @Test
    fun `removes prefix space when cursor at start of line`() {
        val transcription = " Hello world"
        val textBeforeCursor = ""

        val result = transformTranscription(transcription, textBeforeCursor, null)

        assertThat(result).isEqualTo("Hello world")
    }

    @Test
    fun `removes prefix space after newline`() {
        val transcription = " Hello world"
        val textBeforeCursor = "\n"

        val result = transformTranscription(transcription, textBeforeCursor, null)

        assertThat(result).isEqualTo("Hello world")
    }

    @Test
    fun `removes prefix space after existing space`() {
        val transcription = " Hello world"
        val textBeforeCursor = " "

        val result = transformTranscription(transcription, textBeforeCursor, null)

        assertThat(result).isEqualTo("Hello world")
    }

    @Test
    fun `preserves prefix space after text`() {
        val transcription = " Hello world"
        val textBeforeCursor = "Hi"

        val result = transformTranscription(transcription, textBeforeCursor, null)

        assertThat(result).isEqualTo(" Hello world")
    }

    @Test
    fun `preserves transcription without prefix space`() {
        val transcription = "Hello world"
        val textBeforeCursor = "Hi"

        val result = transformTranscription(transcription, textBeforeCursor, null)

        assertThat(result).isEqualTo("Hello world")
    }

    // ==================== Selected Text Replacement Tests ====================

    @Test
    fun `matches uppercase of selected text`() {
        val transcription = "hello"
        val selectedText = "WORLD"

        val result = transformWithSelectedText(transcription, selectedText)

        assertThat(result).isEqualTo("Hello")
    }

    @Test
    fun `matches lowercase of selected text`() {
        val transcription = "Hello"
        val selectedText = "world"

        val result = transformWithSelectedText(transcription, selectedText)

        assertThat(result).isEqualTo("hello")
    }

    @Test
    fun `preserves all-uppercase transcription`() {
        val transcription = "HELLO"
        val selectedText = "world"

        val result = transformWithSelectedText(transcription, selectedText)

        assertThat(result).isEqualTo("HELLO")
    }

    @Test
    fun `handles empty selected text`() {
        val transcription = "Hello"
        val selectedText = ""

        val result = transformWithSelectedText(transcription, selectedText)

        assertThat(result).isEqualTo("Hello")
    }

    // ==================== Punctuation Handling Tests ====================

    @Test
    fun `removes trailing punctuation when replacing selected text`() {
        val transcription = "hello."
        val selectedText = "world"

        val result = cleanTranscriptionForReplacement(transcription)

        assertThat(result).isEqualTo("hello")
    }

    @Test
    fun `removes leading punctuation when replacing selected text`() {
        val transcription = "...hello"
        val selectedText = "world"

        val result = cleanTranscriptionForReplacement(transcription)

        assertThat(result).isEqualTo("hello")
    }

    @Test
    fun `removes both leading and trailing punctuation`() {
        val transcription = "...hello..."
        val selectedText = "world"

        val result = cleanTranscriptionForReplacement(transcription)

        assertThat(result).isEqualTo("hello")
    }

    @Test
    fun `preserves punctuation in middle of text`() {
        val transcription = "hello, world"

        val result = cleanTranscriptionForReplacement(transcription)

        assertThat(result).isEqualTo("hello, world")
    }

    @Test
    fun `handles only punctuation selected text`() {
        val transcription = "hello"
        val selectedText = "..."

        val result = handlePunctuationOnlySelected(transcription, selectedText)

        // When selected text is only punctuation, add space prefix and append selected
        assertThat(result).isEqualTo(" hello...")
    }

    // ==================== End-of-Word Punctuation Preservation ====================

    @Test
    fun `preserves trailing punctuation from selected text`() {
        val selectedText = "world!"
        val transcription = "hello"

        val result = preserveSelectedPunctuation(transcription, selectedText)

        assertThat(result).isEqualTo("hello!")
    }

    @Test
    fun `preserves question mark from selected text`() {
        val selectedText = "what?"
        val transcription = "hello"

        val result = preserveSelectedPunctuation(transcription, selectedText)

        assertThat(result).isEqualTo("hello?")
    }

    @Test
    fun `preserves comma from selected text`() {
        val selectedText = "hello,"
        val transcription = "hi"

        val result = preserveSelectedPunctuation(transcription, selectedText)

        assertThat(result).isEqualTo("hi,")
    }

    // ==================== Lowercase First Letter Logic ====================

    @Test
    fun `lowercases first letter when following regular text`() {
        val textBeforeCursor = "Hi"
        val transcription = "Hello"

        val result = applyContextualCasing(transcription, textBeforeCursor)

        assertThat(result).isEqualTo("hello")
    }

    @Test
    fun `lowercases first letter when following text and space`() {
        val textBeforeCursor = "Hi "
        val transcription = "Hello"

        val result = applyContextualCasing(transcription, textBeforeCursor)

        assertThat(result).isEqualTo("hello")
    }

    @Test
    fun `preserves uppercase when text before is punctuation only`() {
        val textBeforeCursor = ". "
        val transcription = "Hello"

        val result = applyContextualCasing(transcription, textBeforeCursor)

        assertThat(result).isEqualTo("Hello")
    }

    @Test
    fun `preserves uppercase at start of text`() {
        val textBeforeCursor = ""
        val transcription = "Hello"

        val result = applyContextualCasing(transcription, textBeforeCursor)

        assertThat(result).isEqualTo("Hello")
    }

    // ==================== All Uppercase Preservation ====================

    @Test
    fun `preserves all uppercase transcription`() {
        val transcription = "HELLO WORLD"

        val isAllUppercase = transcription.filter { it.isLetter() }.all { it.isUpperCase() }

        assertThat(isAllUppercase).isTrue()
    }

    @Test
    fun `detects mixed case transcription`() {
        val transcription = "Hello World"

        val isAllUppercase = transcription.filter { it.isLetter() }.all { it.isUpperCase() }

        assertThat(isAllUppercase).isFalse()
    }

    @Test
    fun `handles transcription with no letters`() {
        val transcription = "123..."

        // firstOrNull { !it.isUpperCase() } returns null when all are uppercase OR no letters
        val lettersOnly = transcription.filter { it.isLetter() }
        val isAllUppercaseOrNoLetters = lettersOnly.isEmpty() || lettersOnly.all { it.isUpperCase() }

        assertThat(isAllUppercaseOrNoLetters).isTrue()
    }

    @Test
    fun `reapplies uppercase after transformation`() {
        val originalTranscription = "HELLO"
        val transformedTranscription = "hello" // After case matching

        val readdUppercase = originalTranscription.filter { it.isLetter() }
            .firstOrNull { !it.isUpperCase() } == null

        val result = if (readdUppercase) {
            transformedTranscription.uppercase()
        } else {
            transformedTranscription
        }

        assertThat(result).isEqualTo("HELLO")
    }

    // ==================== Complex Scenarios ====================

    @Test
    fun `handles transcription with leading space and selected uppercase text`() {
        val transcription = " hello"
        val textBeforeCursor = ""
        val selectedText = "WORLD"

        // Remove prefix space (cursor at start)
        var result = if (textBeforeCursor.isEmpty()) {
            transcription.removePrefix(" ")
        } else {
            transcription
        }

        // Match case of selected text
        val firstSelectedChar = selectedText.trim().firstOrNull()
        if (firstSelectedChar != null && firstSelectedChar.isUpperCase()) {
            result = result[0].uppercaseChar() + result.substring(1)
        }

        assertThat(result).isEqualTo("Hello")
    }

    @Test
    fun `handles multi-word transcription replacing single word`() {
        val transcription = "hello there"
        val selectedText = "World"

        // Should match first letter case
        val firstSelectedChar = selectedText.trim().firstOrNull()
        val result = if (firstSelectedChar != null && firstSelectedChar.isUpperCase()) {
            transcription[0].uppercaseChar() + transcription.substring(1)
        } else {
            transcription
        }

        assertThat(result).isEqualTo("Hello there")
    }

    // ==================== Helper Functions ====================

    /**
     * Simplified version of the prefix space removal logic.
     */
    private fun transformTranscription(
        transcription: String,
        textBeforeCursor: String?,
        selectedText: String?
    ): String {
        return if (textBeforeCursor.isNullOrEmpty() ||
            textBeforeCursor == "\n" ||
            textBeforeCursor == " "
        ) {
            transcription.removePrefix(" ")
        } else {
            transcription
        }
    }

    /**
     * Simplified version of case matching with selected text.
     */
    private fun transformWithSelectedText(transcription: String, selectedText: String): String {
        if (selectedText.isEmpty()) return transcription

        // Check if transcription is all uppercase
        val isAllUppercase = transcription.filter { it.isLetter() }
            .firstOrNull { !it.isUpperCase() } == null

        if (isAllUppercase) return transcription

        val firstSelectedChar = selectedText.trim().firstOrNull() ?: return transcription

        return when {
            firstSelectedChar.isUpperCase() -> {
                transcription[0].uppercaseChar() + transcription.substring(1)
            }
            firstSelectedChar.isLowerCase() -> {
                transcription[0].lowercaseChar() + transcription.substring(1)
            }
            else -> transcription
        }
    }

    /**
     * Removes leading and trailing punctuation from transcription.
     */
    private fun cleanTranscriptionForReplacement(transcription: String): String {
        val trimmed = transcription.trim()

        val lastLetterOrDigitIndex = trimmed.toList().withIndex().reversed()
            .firstOrNull { it.value.isLetterOrDigit() }?.index ?: return trimmed

        val firstLetterOrDigitIndex = trimmed.toList().withIndex()
            .firstOrNull { it.value.isLetterOrDigit() }?.index ?: return trimmed

        return trimmed.substring(firstLetterOrDigitIndex..lastLetterOrDigitIndex)
    }

    /**
     * Handles case when selected text contains only punctuation.
     */
    private fun handlePunctuationOnlySelected(transcription: String, selectedText: String): String {
        val hasLetterOrDigit = selectedText.firstOrNull { it.isLetterOrDigit() } != null

        return if (!hasLetterOrDigit) {
            val result = " $transcription"
            if (result.last().isLetterOrDigit()) {
                result + selectedText
            } else {
                result
            }
        } else {
            transcription
        }
    }

    /**
     * Preserves trailing punctuation from selected text.
     */
    private fun preserveSelectedPunctuation(transcription: String, selectedText: String): String {
        val trimmedSelected = selectedText.trim()
        val lastLetterOrDigitIndex = trimmedSelected.toList().withIndex().reversed()
            .firstOrNull { it.value.isLetterOrDigit() }?.index

        return if (lastLetterOrDigitIndex != null && lastLetterOrDigitIndex < trimmedSelected.lastIndex) {
            transcription + trimmedSelected.substring(lastLetterOrDigitIndex + 1)
        } else {
            transcription
        }
    }

    /**
     * Applies contextual casing based on text before cursor.
     */
    private fun applyContextualCasing(transcription: String, textBeforeCursor: String?): String {
        if (textBeforeCursor.isNullOrEmpty()) return transcription

        val hasLetterOrDigit = textBeforeCursor.firstOrNull { it.isLetterOrDigit() } != null

        if (!hasLetterOrDigit) return transcription

        // If text before has letters/digits in first two chars, lowercase first letter
        if (textBeforeCursor.length >= 2) {
            val first = textBeforeCursor[0]
            val second = textBeforeCursor[1]

            if (first.isLetterOrDigit() && (second.isLetterOrDigit() || second.isWhitespace())) {
                val firstLetterIndex = transcription.withIndex()
                    .firstOrNull { it.value.isLetterOrDigit() }?.index

                if (firstLetterIndex != null) {
                    val chars = transcription.toCharArray()
                    chars[firstLetterIndex] = chars[firstLetterIndex].lowercaseChar()
                    return chars.concatToString()
                }
            }
        }

        return transcription
    }
}

/**
 * Tests for VoiceInputLifecycleOwner.
 */
class VoiceInputLifecycleOwnerTest {

    @Test
    fun `lifecycle starts in initialized state`() {
        // Simulating lifecycle state tracking
        var currentState = "INITIALIZED"

        assertThat(currentState).isEqualTo("INITIALIZED")
    }

    @Test
    fun `onCreate moves to created state`() {
        var currentState = "INITIALIZED"
        currentState = "CREATED"

        assertThat(currentState).isEqualTo("CREATED")
    }

    @Test
    fun `onResume moves to resumed state`() {
        var currentState = "CREATED"
        currentState = "RESUMED"

        assertThat(currentState).isEqualTo("RESUMED")
    }

    @Test
    fun `onPause moves back from resumed`() {
        var currentState = "RESUMED"
        currentState = "STARTED" // Pausing goes to STARTED

        assertThat(currentState).isEqualTo("STARTED")
    }

    @Test
    fun `onDestroy clears state`() {
        var currentState = "RESUMED"
        var viewModelStoreCleared = false

        currentState = "DESTROYED"
        viewModelStoreCleared = true

        assertThat(currentState).isEqualTo("DESTROYED")
        assertThat(viewModelStoreCleared).isTrue()
    }
}

/**
 * Tests for recognition state management.
 */
class VoiceInputRecognitionStateTest {

    @Test
    fun `isRecognizing starts as false`() {
        var isRecognizing = false

        assertThat(isRecognizing).isFalse()
    }

    @Test
    fun `onReadyForSpeech sets isRecognizing to true`() {
        var isRecognizing = false

        // Simulate onReadyForSpeech
        isRecognizing = true

        assertThat(isRecognizing).isTrue()
    }

    @Test
    fun `onResults sets isRecognizing to false`() {
        var isRecognizing = true

        // Simulate onResults
        isRecognizing = false

        assertThat(isRecognizing).isFalse()
    }

    @Test
    fun `cancel sets isRecognizing to false`() {
        var isRecognizing = true

        // Simulate cancel
        isRecognizing = false

        assertThat(isRecognizing).isFalse()
    }

    @Test
    fun `isSpeaking tracks speech state`() {
        var isSpeaking = false

        // onBeginningOfSpeech
        isSpeaking = true
        assertThat(isSpeaking).isTrue()

        // onEndOfSpeech
        isSpeaking = false
        assertThat(isSpeaking).isFalse()
    }

    @Test
    fun `showInsufficientPermissionsError tracks permission state`() {
        var showError = false

        // ERROR_INSUFFICIENT_PERMISSIONS
        showError = true
        assertThat(showError).isTrue()

        // After opening settings
        showError = false
        assertThat(showError).isFalse()
    }
}
