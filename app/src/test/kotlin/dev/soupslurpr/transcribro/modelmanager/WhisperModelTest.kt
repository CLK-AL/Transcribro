package dev.soupslurpr.transcribro.modelmanager

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for WhisperModel and AvailableModels.
 */
class WhisperModelTest {

    @Test
    fun `WhisperModel has correct properties`() {
        val model = WhisperModel(
            id = "test-model",
            displayName = "Test Model",
            description = "A test model",
            languages = listOf("en", "es"),
            sizeBytes = 100_000_000L,
            downloadUrl = "https://example.com/model.bin",
            fileName = "model.bin",
            quantization = "q5_0"
        )

        assertThat(model.id).isEqualTo("test-model")
        assertThat(model.displayName).isEqualTo("Test Model")
        assertThat(model.description).isEqualTo("A test model")
        assertThat(model.languages).containsExactly("en", "es")
        assertThat(model.sizeBytes).isEqualTo(100_000_000L)
        assertThat(model.downloadUrl).isEqualTo("https://example.com/model.bin")
        assertThat(model.fileName).isEqualTo("model.bin")
        assertThat(model.quantization).isEqualTo("q5_0")
    }

    @Test
    fun `WhisperModel quantization can be null`() {
        val model = WhisperModel(
            id = "test",
            displayName = "Test",
            description = "Test",
            languages = listOf("en"),
            sizeBytes = 1000L,
            downloadUrl = "https://example.com/model.bin",
            fileName = "model.bin"
        )

        assertThat(model.quantization).isNull()
    }

    @Test
    fun `AvailableModels has all expected models`() {
        val models = AvailableModels.ALL_MODELS

        assertThat(models).isNotEmpty()
        // Should contain tiny models
        assertThat(models.map { it.id }).contains("tiny-q5_0")
        assertThat(models.map { it.id }).contains("tiny.en-q5_0")
        // Should contain base models
        assertThat(models.map { it.id }).contains("base-q5_0")
        // Should contain Hebrew models
        assertThat(models.map { it.id }).contains("ivrit-v2-d4-q2_k")
    }

    @Test
    fun `AvailableModels getDefault returns Tiny Multilingual`() {
        val defaultModel = AvailableModels.getDefault()

        assertThat(defaultModel.id).isEqualTo("tiny-q5_0")
        assertThat(defaultModel.displayName).isEqualTo("Tiny Multilingual")
        assertThat(defaultModel.languages).contains("multilingual")
    }

    @Test
    fun `AvailableModels getById returns correct model`() {
        val model = AvailableModels.getById("tiny-q5_0")

        assertThat(model).isNotNull()
        assertThat(model!!.displayName).isEqualTo("Tiny Multilingual")
    }

    @Test
    fun `AvailableModels getById returns null for unknown id`() {
        val model = AvailableModels.getById("nonexistent-model")

        assertThat(model).isNull()
    }

    @Test
    fun `AvailableModels formatSize formats bytes correctly`() {
        assertThat(AvailableModels.formatSize(500)).isEqualTo("500 B")
        assertThat(AvailableModels.formatSize(1_500)).isEqualTo("2 KB")
        assertThat(AvailableModels.formatSize(26_000_000)).isEqualTo("26 MB")
        assertThat(AvailableModels.formatSize(1_080_000_000)).isEqualTo("1.1 GB")
    }

    @Test
    fun `All models have valid download URLs`() {
        AvailableModels.ALL_MODELS.forEach { model ->
            assertThat(model.downloadUrl).startsWith("https://huggingface.co/")
            assertThat(model.downloadUrl).endsWith(".bin")
        }
    }

    @Test
    fun `All models have unique IDs`() {
        val ids = AvailableModels.ALL_MODELS.map { it.id }
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `All models have unique file names`() {
        val fileNames = AvailableModels.ALL_MODELS.map { it.fileName }
        assertThat(fileNames).containsNoDuplicates()
    }

    @Test
    fun `All models have positive size`() {
        AvailableModels.ALL_MODELS.forEach { model ->
            assertThat(model.sizeBytes).isGreaterThan(0L)
        }
    }

    @Test
    fun `All models have non-empty languages`() {
        AvailableModels.ALL_MODELS.forEach { model ->
            assertThat(model.languages).isNotEmpty()
        }
    }

    @Test
    fun `Multilingual models have correct language tag`() {
        val multilingualModels = AvailableModels.ALL_MODELS.filter {
            it.displayName.contains("Multilingual") || it.displayName.contains("Large")
        }

        multilingualModels.forEach { model ->
            if (!model.displayName.contains("Hebrew")) {
                assertThat(model.languages).contains("multilingual")
            }
        }
    }

    @Test
    fun `English-only models have en language tag`() {
        val englishModels = AvailableModels.ALL_MODELS.filter {
            it.displayName.contains("English") && !it.displayName.contains("Hebrew")
        }

        englishModels.forEach { model ->
            assertThat(model.languages).contains("en")
        }
    }

    @Test
    fun `Hebrew models have he language tag`() {
        val hebrewModels = AvailableModels.ALL_MODELS.filter {
            it.displayName.contains("Hebrew")
        }

        assertThat(hebrewModels).isNotEmpty()
        hebrewModels.forEach { model ->
            assertThat(model.languages).contains("he")
        }
    }

    @Test
    fun `Default model is first in the list`() {
        val firstModel = AvailableModels.ALL_MODELS.first()
        val defaultModel = AvailableModels.getDefault()

        assertThat(firstModel).isEqualTo(defaultModel)
    }
}
