package dev.soupslurpr.transcribro

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Transcribro navigation logic.
 *
 * Tests cover:
 * - TranscribroAppScreens enum values and properties
 * - Navigation bar screen filtering
 * - Screen ordinal-based transition direction logic
 */
class TranscribroNavigationTest {

    // ==================== TranscribroAppScreens Enum Tests ====================

    @Test
    fun `all screen enum values are defined`() {
        val screens = TranscribroAppScreens.values()

        assertThat(screens).hasLength(10)
    }

    @Test
    fun `ActionRecognizeSpeech screen exists`() {
        val screen = TranscribroAppScreens.ActionRecognizeSpeech

        assertThat(screen.name).isEqualTo("ActionRecognizeSpeech")
    }

    @Test
    fun `Start screen has correct title resource`() {
        val screen = TranscribroAppScreens.Start

        assertThat(screen.title).isEqualTo(R.string.start)
    }

    @Test
    fun `Settings screen has correct title resource`() {
        val screen = TranscribroAppScreens.Settings

        assertThat(screen.title).isEqualTo(R.string.settings)
    }

    @Test
    fun `Donate screen has correct title resource`() {
        val screen = TranscribroAppScreens.Donate

        assertThat(screen.title).isEqualTo(R.string.donate)
    }

    @Test
    fun `SettingsLicense screen has correct title resource`() {
        val screen = TranscribroAppScreens.SettingsLicense

        assertThat(screen.title).isEqualTo(R.string.license)
    }

    @Test
    fun `SettingsPrivacyPolicy screen has correct title resource`() {
        val screen = TranscribroAppScreens.SettingsPrivacyPolicy

        assertThat(screen.title).isEqualTo(R.string.privacy_policy)
    }

    @Test
    fun `SettingsCredits screen has correct title resource`() {
        val screen = TranscribroAppScreens.SettingsCredits

        assertThat(screen.title).isEqualTo(R.string.credits)
    }

    // ==================== Navigation Bar Tests ====================

    @Test
    fun `navBarScreens contains exactly three screens`() {
        assertThat(navBarScreens).hasSize(3)
    }

    @Test
    fun `navBarScreens contains Start`() {
        assertThat(navBarScreens).contains(TranscribroAppScreens.Start)
    }

    @Test
    fun `navBarScreens contains Settings`() {
        assertThat(navBarScreens).contains(TranscribroAppScreens.Settings)
    }

    @Test
    fun `navBarScreens contains Donate`() {
        assertThat(navBarScreens).contains(TranscribroAppScreens.Donate)
    }

    @Test
    fun `navBarScreens order is Start, Settings, Donate`() {
        assertThat(navBarScreens[0]).isEqualTo(TranscribroAppScreens.Start)
        assertThat(navBarScreens[1]).isEqualTo(TranscribroAppScreens.Settings)
        assertThat(navBarScreens[2]).isEqualTo(TranscribroAppScreens.Donate)
    }

    // ==================== Screen Ordinal Tests (for transition direction) ====================

    @Test
    fun `ActionRecognizeSpeech has lowest ordinal`() {
        assertThat(TranscribroAppScreens.ActionRecognizeSpeech.ordinal).isEqualTo(0)
    }

    @Test
    fun `Start ordinal is less than Settings ordinal`() {
        assertThat(TranscribroAppScreens.Start.ordinal)
            .isLessThan(TranscribroAppScreens.Settings.ordinal)
    }

    @Test
    fun `Settings ordinal is less than Donate ordinal`() {
        assertThat(TranscribroAppScreens.Settings.ordinal)
            .isLessThan(TranscribroAppScreens.Donate.ordinal)
    }

    @Test
    fun `SettingsStart ordinal is between Settings and SettingsLicense`() {
        assertThat(TranscribroAppScreens.SettingsStart.ordinal)
            .isGreaterThan(TranscribroAppScreens.Settings.ordinal)
        assertThat(TranscribroAppScreens.SettingsStart.ordinal)
            .isLessThan(TranscribroAppScreens.SettingsLicense.ordinal)
    }

    // ==================== Transition Direction Logic Tests ====================

    @Test
    fun `navigating from Start to Settings slides right (positive direction)`() {
        val initialOrdinal = TranscribroAppScreens.Start.ordinal
        val targetOrdinal = TranscribroAppScreens.Settings.ordinal

        // When initial < target, slide from right (positive width)
        val slideFromRight = initialOrdinal < targetOrdinal

        assertThat(slideFromRight).isTrue()
    }

    @Test
    fun `navigating from Settings to Start slides left (negative direction)`() {
        val initialOrdinal = TranscribroAppScreens.Settings.ordinal
        val targetOrdinal = TranscribroAppScreens.Start.ordinal

        // When initial > target, slide from left (negative width)
        val slideFromLeft = initialOrdinal > targetOrdinal

        assertThat(slideFromLeft).isTrue()
    }

    @Test
    fun `navigating from Start to Donate slides right`() {
        val initialOrdinal = TranscribroAppScreens.Start.ordinal
        val targetOrdinal = TranscribroAppScreens.Donate.ordinal

        val slideFromRight = initialOrdinal < targetOrdinal

        assertThat(slideFromRight).isTrue()
    }

    @Test
    fun `navigating from Donate to Start slides left`() {
        val initialOrdinal = TranscribroAppScreens.Donate.ordinal
        val targetOrdinal = TranscribroAppScreens.Start.ordinal

        val slideFromLeft = initialOrdinal > targetOrdinal

        assertThat(slideFromLeft).isTrue()
    }

    // ==================== Screen Name Matching Tests ====================

    @Test
    fun `valueOf parses Start correctly`() {
        val screen = TranscribroAppScreens.valueOf("Start")

        assertThat(screen).isEqualTo(TranscribroAppScreens.Start)
    }

    @Test
    fun `valueOf parses Settings correctly`() {
        val screen = TranscribroAppScreens.valueOf("Settings")

        assertThat(screen).isEqualTo(TranscribroAppScreens.Settings)
    }

    @Test
    fun `valueOf parses SettingsLicense correctly`() {
        val screen = TranscribroAppScreens.valueOf("SettingsLicense")

        assertThat(screen).isEqualTo(TranscribroAppScreens.SettingsLicense)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `valueOf throws for invalid screen name`() {
        TranscribroAppScreens.valueOf("InvalidScreen")
    }

    // ==================== NavBar Selection Logic Tests ====================

    @Test
    fun `StartStart route starts with Start`() {
        val route = TranscribroAppScreens.StartStart.name

        assertThat(route.startsWith("Start")).isTrue()
    }

    @Test
    fun `SettingsStart route starts with Settings`() {
        val route = TranscribroAppScreens.SettingsStart.name

        assertThat(route.startsWith("Settings")).isTrue()
    }

    @Test
    fun `SettingsLicense route starts with Settings`() {
        val route = TranscribroAppScreens.SettingsLicense.name

        assertThat(route.startsWith("Settings")).isTrue()
    }

    @Test
    fun `DonateStart route starts with Donate`() {
        val route = TranscribroAppScreens.DonateStart.name

        assertThat(route.startsWith("Donate")).isTrue()
    }

    @Test
    fun `findNavBarScreen returns Settings for SettingsLicense`() {
        val currentScreen = TranscribroAppScreens.SettingsLicense

        val navBarSelected = navBarScreens.find {
            currentScreen.name.startsWith(it.name)
        }

        assertThat(navBarSelected).isEqualTo(TranscribroAppScreens.Settings)
    }

    @Test
    fun `findNavBarScreen returns Start for StartStart`() {
        val currentScreen = TranscribroAppScreens.StartStart

        val navBarSelected = navBarScreens.find {
            currentScreen.name.startsWith(it.name)
        }

        assertThat(navBarSelected).isEqualTo(TranscribroAppScreens.Start)
    }

    @Test
    fun `findNavBarScreen returns null for ActionRecognizeSpeech`() {
        val currentScreen = TranscribroAppScreens.ActionRecognizeSpeech

        val navBarSelected = navBarScreens.find {
            currentScreen.name.startsWith(it.name)
        }

        assertThat(navBarSelected).isNull()
    }

    // ==================== Route Name Tests ====================

    @Test
    fun `screen names match enum names exactly`() {
        TranscribroAppScreens.values().forEach { screen ->
            assertThat(screen.name).isEqualTo(screen.toString())
        }
    }

    @Test
    fun `all settings sub-screens have Settings prefix`() {
        val settingsScreens = listOf(
            TranscribroAppScreens.Settings,
            TranscribroAppScreens.SettingsStart,
            TranscribroAppScreens.SettingsLicense,
            TranscribroAppScreens.SettingsPrivacyPolicy,
            TranscribroAppScreens.SettingsCredits
        )

        settingsScreens.forEach { screen ->
            assertThat(screen.name.startsWith("Settings")).isTrue()
        }
    }
}
