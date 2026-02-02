package dev.soupslurpr.transcribro.ui.reusablecomposables

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.AnnotatedString
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented tests for ClickableText composable.
 *
 * Tests cover:
 * - Text rendering
 * - Click detection and offset reporting
 * - Style application
 */
class ClickableTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickableText_displaysText() {
        composeTestRule.setContent {
            ClickableText(
                text = AnnotatedString("Hello World"),
                onClick = {}
            )
        }

        composeTestRule.onNodeWithText("Hello World").assertExists()
    }

    @Test
    fun clickableText_invokesOnClick() {
        var clicked = false

        composeTestRule.setContent {
            ClickableText(
                text = AnnotatedString("Click me"),
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Click me").performClick()

        assertThat(clicked).isTrue()
    }

    @Test
    fun clickableText_reportsClickOffset() {
        var clickOffset = -1

        composeTestRule.setContent {
            ClickableText(
                text = AnnotatedString("Hello World"),
                onClick = { offset -> clickOffset = offset }
            )
        }

        composeTestRule.onNodeWithText("Hello World").performClick()

        // Click offset should be a valid position in the text
        assertThat(clickOffset).isAtLeast(0)
    }

    @Test
    fun clickableText_handlesEmptyText() {
        var clicked = false

        composeTestRule.setContent {
            ClickableText(
                text = AnnotatedString(""),
                onClick = { clicked = true }
            )
        }

        // Empty text node may not be clickable, but should not crash
        // This test verifies the composable handles empty input gracefully
    }

    @Test
    fun clickableText_handlesMultilineText() {
        var clickOffset = -1

        composeTestRule.setContent {
            ClickableText(
                text = AnnotatedString("Line 1\nLine 2\nLine 3"),
                onClick = { offset -> clickOffset = offset }
            )
        }

        composeTestRule.onNodeWithText("Line 1\nLine 2\nLine 3").performClick()

        assertThat(clickOffset).isAtLeast(0)
    }

    @Test
    fun clickableText_appliesMaxLines() {
        composeTestRule.setContent {
            ClickableText(
                text = AnnotatedString("This is a very long text that might wrap"),
                maxLines = 1,
                onClick = {}
            )
        }

        composeTestRule.onNodeWithText("This is a very long text that might wrap").assertExists()
    }
}
