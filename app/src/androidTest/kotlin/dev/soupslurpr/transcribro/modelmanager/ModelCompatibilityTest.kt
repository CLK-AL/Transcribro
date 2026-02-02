package dev.soupslurpr.transcribro.modelmanager

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.whispercpp.whisper.WhisperContext
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Integration tests that verify model compatibility by downloading and loading each model.
 *
 * These tests are marked with @LargeTest equivalent behavior:
 * - They require network access
 * - They download large files
 * - They load native libraries
 *
 * Run these tests selectively, not as part of CI, due to:
 * - Large download sizes (26MB - 1GB+ per model)
 * - Network dependency
 * - Long execution time
 */
@RunWith(AndroidJUnit4::class)
class ModelCompatibilityTest {

    companion object {
        private const val TAG = "ModelCompatibilityTest"
    }

    private lateinit var modelDownloadManager: ModelDownloadManager
    private lateinit var modelsDir: File
    private val downloadedModels = mutableListOf<WhisperModel>()

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        modelDownloadManager = ModelDownloadManager(context)
        modelsDir = File(context.filesDir, "whisper_models")
    }

    @After
    fun cleanup() {
        // Clean up downloaded test models to save space
        downloadedModels.forEach { model ->
            try {
                modelDownloadManager.deleteModel(model)
                Log.d(TAG, "Cleaned up: ${model.fileName}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clean up ${model.fileName}: ${e.message}")
            }
        }
        downloadedModels.clear()
    }

    /**
     * Test downloading and loading the default model (Tiny Multilingual Q5).
     * This is the most important test as it's the default model users will use.
     */
    @Test
    fun testDefaultModel_downloadAndLoad() = runBlocking {
        val model = AvailableModels.getDefault()
        testModelDownloadAndLoad(model)
    }

    /**
     * Test downloading and loading Tiny English Q5 model.
     */
    @Test
    fun testTinyEnglish_downloadAndLoad() = runBlocking {
        val model = AvailableModels.TINY_EN
        testModelDownloadAndLoad(model)
    }

    /**
     * Test downloading and loading Tiny Multilingual Q8 model.
     */
    @Test
    fun testTinyMultilingualQ8_downloadAndLoad() = runBlocking {
        val model = AvailableModels.TINY_MULTILINGUAL_Q8
        testModelDownloadAndLoad(model)
    }

    /**
     * Test downloading and loading Base English model.
     */
    @Test
    fun testBaseEnglish_downloadAndLoad() = runBlocking {
        val model = AvailableModels.BASE_EN
        testModelDownloadAndLoad(model)
    }

    /**
     * Test downloading and loading Base Multilingual model.
     */
    @Test
    fun testBaseMultilingual_downloadAndLoad() = runBlocking {
        val model = AvailableModels.BASE_MULTILINGUAL
        testModelDownloadAndLoad(model)
    }

    /**
     * Test downloading and loading Small English model.
     */
    @Test
    fun testSmallEnglish_downloadAndLoad() = runBlocking {
        val model = AvailableModels.SMALL_EN
        testModelDownloadAndLoad(model)
    }

    /**
     * Test downloading and loading Small Multilingual model.
     */
    @Test
    fun testSmallMultilingual_downloadAndLoad() = runBlocking {
        val model = AvailableModels.SMALL_MULTILINGUAL
        testModelDownloadAndLoad(model)
    }

    /**
     * Test downloading and loading Hebrew Ivrit Q2K model.
     */
    @Test
    fun testHebrewIvritQ2K_downloadAndLoad() = runBlocking {
        val model = AvailableModels.HEBREW_IVRIT_Q2K
        testModelDownloadAndLoad(model)
    }

    /**
     * Verify all model URLs are accessible (HEAD request only, no download).
     */
    @Test
    fun testAllModelUrls_areAccessible() = runBlocking {
        AvailableModels.ALL_MODELS.forEach { model ->
            Log.d(TAG, "Checking URL accessibility: ${model.displayName}")

            val url = java.net.URL(model.downloadUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 15_000
            connection.setRequestProperty("User-Agent", "Transcribro-Android-Test")

            try {
                val responseCode = connection.responseCode
                assertThat(responseCode).isEqualTo(200)
                Log.d(TAG, "  URL OK: ${model.downloadUrl}")
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * Verify model file sizes match expected sizes (within tolerance).
     */
    @Test
    fun testModelFileSizes_matchExpected() = runBlocking {
        // Only test the default model to keep test quick
        val model = AvailableModels.getDefault()

        // Download if not already downloaded
        if (!modelDownloadManager.isModelDownloaded(model)) {
            val result = modelDownloadManager.downloadModel(model)
            assertThat(result.isSuccess).isTrue()
            downloadedModels.add(model)
        }

        val modelFile = File(modelsDir, model.fileName)
        assertThat(modelFile.exists()).isTrue()

        // File size should be within 20% of expected
        val actualSize = modelFile.length()
        val expectedSize = model.sizeBytes
        val tolerance = expectedSize * 0.2

        assertThat(actualSize).isWithin(tolerance.toLong()).of(expectedSize)
        Log.d(TAG, "Size check: expected=${AvailableModels.formatSize(expectedSize)}, " +
                "actual=${AvailableModels.formatSize(actualSize)}")
    }

    /**
     * Test that download progress is reported correctly.
     */
    @Test
    fun testDownloadProgress_isReported() = runBlocking {
        val model = AvailableModels.getDefault()

        // Delete if exists to force fresh download
        if (modelDownloadManager.isModelDownloaded(model)) {
            modelDownloadManager.deleteModel(model)
        }

        var progressReported = false
        var lastProgress = 0f

        // Collect download states
        val statesJob = kotlinx.coroutines.launch {
            modelDownloadManager.downloadStates.collect { states ->
                val state = states[model.id]
                if (state is DownloadState.Downloading) {
                    progressReported = true
                    lastProgress = state.progress
                    Log.d(TAG, "Download progress: ${(state.progress * 100).toInt()}%")
                }
            }
        }

        val result = modelDownloadManager.downloadModel(model)
        statesJob.cancel()

        assertThat(result.isSuccess).isTrue()
        assertThat(progressReported).isTrue()
        downloadedModels.add(model)
    }

    /**
     * Test model deletion.
     */
    @Test
    fun testModelDeletion() = runBlocking {
        val model = AvailableModels.getDefault()

        // Download first
        if (!modelDownloadManager.isModelDownloaded(model)) {
            val result = modelDownloadManager.downloadModel(model)
            assertThat(result.isSuccess).isTrue()
        }

        assertThat(modelDownloadManager.isModelDownloaded(model)).isTrue()

        // Delete
        val deleted = modelDownloadManager.deleteModel(model)
        assertThat(deleted).isTrue()
        assertThat(modelDownloadManager.isModelDownloaded(model)).isFalse()
    }

    /**
     * Helper function to download and load a model.
     */
    private suspend fun testModelDownloadAndLoad(model: WhisperModel) {
        Log.d(TAG, "Testing model: ${model.displayName} (${model.fileName})")
        Log.d(TAG, "  Expected size: ${AvailableModels.formatSize(model.sizeBytes)}")
        Log.d(TAG, "  Download URL: ${model.downloadUrl}")

        // Step 1: Download the model
        Log.d(TAG, "  Downloading...")
        val downloadResult = modelDownloadManager.downloadModel(model)

        assertThat(downloadResult.isSuccess)
            .withFailureMessage("Failed to download ${model.displayName}: ${downloadResult.exceptionOrNull()?.message}")
            .isTrue()

        downloadedModels.add(model)

        // Step 2: Verify file exists
        val modelFile = downloadResult.getOrNull()!!
        assertThat(modelFile.exists()).isTrue()
        assertThat(modelFile.length()).isGreaterThan(0L)
        Log.d(TAG, "  Downloaded: ${AvailableModels.formatSize(modelFile.length())}")

        // Step 3: Load the model with WhisperContext
        Log.d(TAG, "  Loading into WhisperContext...")
        var whisperContext: WhisperContext? = null
        try {
            whisperContext = WhisperContext.createContextFromFile(modelFile.absolutePath)
            assertThat(whisperContext).isNotNull()
            Log.d(TAG, "  Model loaded successfully!")
        } catch (e: Exception) {
            throw AssertionError("Failed to load ${model.displayName} into WhisperContext: ${e.message}", e)
        } finally {
            // Release the context
            try {
                whisperContext?.release()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to release context: ${e.message}")
            }
        }
    }
}
