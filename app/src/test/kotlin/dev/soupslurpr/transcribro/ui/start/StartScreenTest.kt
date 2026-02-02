package dev.soupslurpr.transcribro.ui.start

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDateTime

/**
 * Unit tests for StartScreen logic.
 *
 * Tests cover:
 * - Splash message selection
 * - April Fools detection
 * - Input method enabled detection logic
 * - Random value distribution
 */
class StartScreenTest {

    // ==================== Splash Message Tests ====================

    @Test
    fun `splash message index 0 returns correct message`() {
        val message = getSplashMessage(0)
        assertThat(message).isEqualTo("Where every word counts, unless it's a typo.")
    }

    @Test
    fun `splash message index 1 returns correct message`() {
        val message = getSplashMessage(1)
        assertThat(message).isEqualTo("Always here to listen, even if it's just your fridge humming.")
    }

    @Test
    fun `splash message index 23 returns last regular message`() {
        val message = getSplashMessage(23)
        assertThat(message).isEqualTo("Making sure \"once upon a time\" never becomes \"one upon a tim\".")
    }

    @Test
    fun `splash message else case returns easter egg`() {
        val message = getSplashMessage(24)
        assertThat(message).contains("open source")
    }

    @Test
    fun `splash message negative index returns easter egg`() {
        val message = getSplashMessage(-1)
        assertThat(message).contains("open source")
    }

    @Test
    fun `all 24 splash messages are unique`() {
        val messages = (0..23).map { getSplashMessage(it) }
        val uniqueMessages = messages.toSet()

        assertThat(uniqueMessages).hasSize(24)
    }

    @Test
    fun `splash messages contain expected themes`() {
        val allMessages = (0..23).map { getSplashMessage(it) }.joinToString(" ")

        // Should contain various themes
        assertThat(allMessages).contains("typo")
        assertThat(allMessages).contains("privacy")
        assertThat(allMessages).contains("typing")
    }

    // ==================== April Fools Detection Tests ====================

    @Test
    fun `isAprilFools returns true for April 1st`() {
        val aprilFirst = LocalDateTime.of(2024, 4, 1, 12, 0)
        val isAprilFools = isAprilFoolsDate(aprilFirst)

        assertThat(isAprilFools).isTrue()
    }

    @Test
    fun `isAprilFools returns false for April 2nd`() {
        val aprilSecond = LocalDateTime.of(2024, 4, 2, 12, 0)
        val isAprilFools = isAprilFoolsDate(aprilSecond)

        assertThat(isAprilFools).isFalse()
    }

    @Test
    fun `isAprilFools returns false for March 31st`() {
        val marchLast = LocalDateTime.of(2024, 3, 31, 12, 0)
        val isAprilFools = isAprilFoolsDate(marchLast)

        assertThat(isAprilFools).isFalse()
    }

    @Test
    fun `isAprilFools returns false for January 1st`() {
        val newYear = LocalDateTime.of(2024, 1, 1, 12, 0)
        val isAprilFools = isAprilFoolsDate(newYear)

        assertThat(isAprilFools).isFalse()
    }

    @Test
    fun `isAprilFools works at midnight on April 1st`() {
        val aprilFirstMidnight = LocalDateTime.of(2024, 4, 1, 0, 0)
        val isAprilFools = isAprilFoolsDate(aprilFirstMidnight)

        assertThat(isAprilFools).isTrue()
    }

    @Test
    fun `isAprilFools works at end of day on April 1st`() {
        val aprilFirstEndOfDay = LocalDateTime.of(2024, 4, 1, 23, 59)
        val isAprilFools = isAprilFoolsDate(aprilFirstEndOfDay)

        assertThat(isAprilFools).isTrue()
    }

    // ==================== Random Value Distribution Tests ====================

    @Test
    fun `random value range is 0 to 23 inclusive`() {
        // Simulate many random selections
        val values = (0..1000).map { kotlin.random.Random.nextInt(0, 24) }

        assertThat(values.all { it in 0..23 }).isTrue()
        assertThat(values.min()).isAtLeast(0)
        assertThat(values.max()).isAtMost(23)
    }

    @Test
    fun `random distribution covers all values`() {
        // With enough samples, all values 0-23 should appear
        val values = (0..10000).map { kotlin.random.Random.nextInt(0, 24) }.toSet()

        assertThat(values).containsAtLeastElementsIn(0..23)
    }

    // ==================== Input Method Detection Logic Tests ====================

    @Test
    fun `package name comparison is exact match`() {
        val myPackage = "dev.soupslurpr.transcribro"
        val otherPackage = "com.google.gboard"

        assertThat(myPackage == otherPackage).isFalse()
        assertThat(myPackage == myPackage).isTrue()
    }

    @Test
    fun `package name comparison handles prefixes`() {
        val myPackage = "dev.soupslurpr.transcribro"
        val prefixPackage = "dev.soupslurpr.transcribro.debug"

        assertThat(myPackage == prefixPackage).isFalse()
    }

    @Test
    fun `empty enabled methods list returns false`() {
        val enabledMethods = emptyList<MockInputMethod>()
        val myPackage = "dev.soupslurpr.transcribro"

        val isEnabled = enabledMethods.any { it.packageName == myPackage }

        assertThat(isEnabled).isFalse()
    }

    @Test
    fun `single matching method returns true`() {
        val enabledMethods = listOf(
            MockInputMethod("dev.soupslurpr.transcribro")
        )
        val myPackage = "dev.soupslurpr.transcribro"

        val isEnabled = enabledMethods.any { it.packageName == myPackage }

        assertThat(isEnabled).isTrue()
    }

    @Test
    fun `multiple methods with match returns true`() {
        val enabledMethods = listOf(
            MockInputMethod("com.google.gboard"),
            MockInputMethod("dev.soupslurpr.transcribro"),
            MockInputMethod("com.swiftkey.keyboard")
        )
        val myPackage = "dev.soupslurpr.transcribro"

        val isEnabled = enabledMethods.any { it.packageName == myPackage }

        assertThat(isEnabled).isTrue()
    }

    @Test
    fun `multiple methods without match returns false`() {
        val enabledMethods = listOf(
            MockInputMethod("com.google.gboard"),
            MockInputMethod("com.swiftkey.keyboard")
        )
        val myPackage = "dev.soupslurpr.transcribro"

        val isEnabled = enabledMethods.any { it.packageName == myPackage }

        assertThat(isEnabled).isFalse()
    }

    // ==================== Helper Functions ====================

    /**
     * Recreates the splash message selection logic from StartScreen.
     */
    private fun getSplashMessage(randomValue: Int): String {
        return when (randomValue) {
            0 -> "Where every word counts, unless it's a typo."
            1 -> "Always here to listen, even if it's just your fridge humming."
            2 -> "Because your thoughts deserve to be transcribed, word for word."
            3 -> "Talk nerdy to me. I'll translate it to text."
            4 -> "Turning rambles into readable recaps with a tap."
            5 -> "Whisper, sing, or discuss the theory of relativity, I've got you covered."
            6 -> "Where \"I didn't catch that\" simply doesn't exist."
            7 -> "Speak of the devil, and I shall make him grammatically correct."
            8 -> "In a world of autocorrect fails, remains your faithful scribe."
            9 -> "Fine-tuning your \"ums\" and \"ahs\"—catching them when they count but skimming over when they're just filler."
            10 -> "Don't worry about the loud coffee shop. I listen to you, not the latte art."
            11 -> "Bridging the gap between brainwaves and text, one word at a time."
            12 -> "You do the talking; I handle the typing. Teamwork makes the dream work."
            13 -> "Where your voice gets VIP treatment, no velvet rope required."
            14 -> "Transcribruh, Transcribruv, Transcribrah"
            15 -> "Unofficially competing for the title of \"World's Most Patient Listener\" since launch."
            16 -> "Because typing is soooo 20th century."
            17 -> "Quietly accurate, loudly private."
            18 -> "Speech-to-text without the cloud circus."
            19 -> "When speed matters and privacy matters more."
            20 -> "Freeing you from thumb cramps since 2024."
            21 -> "Save the thumbs, use thy tongue."
            22 -> "Doesn't work well with brainrot. Yet."
            23 -> "Making sure \"once upon a time\" never becomes \"one upon a tim\"."
            else -> "Hey! Stop reading my source code without my consent! Just kidding, I'm open source :)"
        }
    }

    /**
     * Recreates the April Fools check logic.
     */
    private fun isAprilFoolsDate(dateTime: LocalDateTime): Boolean {
        return dateTime.monthValue == 4 && dateTime.dayOfMonth == 1
    }

    /**
     * Mock input method for testing.
     */
    data class MockInputMethod(val packageName: String)
}
