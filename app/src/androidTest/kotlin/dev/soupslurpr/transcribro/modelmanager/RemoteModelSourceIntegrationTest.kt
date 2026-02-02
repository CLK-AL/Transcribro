package dev.soupslurpr.transcribro.modelmanager

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for RemoteModelSource that verify HuggingFace API access.
 *
 * These tests require network access but don't download large model files.
 */
@RunWith(AndroidJUnit4::class)
class RemoteModelSourceIntegrationTest {

    companion object {
        private const val TAG = "RemoteModelSourceTest"
    }

    /**
     * Test fetching model list from whisper.cpp repository.
     */
    @Test
    fun testFetchWhisperCppModels() = runBlocking {
        Log.d(TAG, "Fetching models from whisper.cpp repo...")

        val result = RemoteModelSource.fetchModelsFromSource(ModelSource.WHISPER_CPP)

        assertThat(result.isSuccess)
            .withFailureMessage("Failed to fetch whisper.cpp models: ${result.exceptionOrNull()?.message}")
            .isTrue()

        val models = result.getOrNull()!!
        assertThat(models).isNotEmpty()

        Log.d(TAG, "Found ${models.size} .bin files in whisper.cpp repo:")
        models.take(10).forEach { file ->
            Log.d(TAG, "  - ${file.fileName} (${AvailableModels.formatSize(file.sizeBytes)})")
        }

        // Verify some expected models are present
        val fileNames = models.map { it.fileName }
        assertThat(fileNames).contains("ggml-tiny-q5_0.bin")
        assertThat(fileNames).contains("ggml-base-q5_0.bin")
    }

    /**
     * Test fetching model list from ivrit-ggml repository.
     */
    @Test
    fun testFetchIvritModels() = runBlocking {
        Log.d(TAG, "Fetching models from ivrit-ggml repo...")

        val result = RemoteModelSource.fetchModelsFromSource(ModelSource.IVRIT_GGML)

        assertThat(result.isSuccess)
            .withFailureMessage("Failed to fetch ivrit models: ${result.exceptionOrNull()?.message}")
            .isTrue()

        val models = result.getOrNull()!!
        assertThat(models).isNotEmpty()

        Log.d(TAG, "Found ${models.size} .bin files in ivrit-ggml repo:")
        models.forEach { file ->
            Log.d(TAG, "  - ${file.fileName} (${AvailableModels.formatSize(file.sizeBytes)})")
        }

        // Verify Hebrew models are present
        val fileNames = models.map { it.fileName }
        assertThat(fileNames.any { it.contains("ivrit") }).isTrue()
    }

    /**
     * Test fetching all remote models.
     */
    @Test
    fun testFetchAllRemoteModels() = runBlocking {
        Log.d(TAG, "Fetching models from all sources...")

        val results = RemoteModelSource.fetchAllRemoteModels()

        assertThat(results).hasSize(ModelSource.entries.size)

        var totalFiles = 0
        results.forEach { (source, result) ->
            if (result.isSuccess) {
                val files = result.getOrNull()!!
                totalFiles += files.size
                Log.d(TAG, "${source.repoName}: ${files.size} files")
            } else {
                Log.w(TAG, "${source.repoName}: Failed - ${result.exceptionOrNull()?.message}")
            }
        }

        Log.d(TAG, "Total: $totalFiles .bin files across all sources")
        assertThat(totalFiles).isGreaterThan(0)
    }

    /**
     * Test that all app models have matching remote files.
     */
    @Test
    fun testAppModels_haveRemoteMatches() = runBlocking {
        Log.d(TAG, "Checking app models against remote sources...")

        val results = RemoteModelSource.fetchAllRemoteModels()
        val allRemoteFiles = results.values
            .mapNotNull { it.getOrNull() }
            .flatten()

        Log.d(TAG, "Found ${allRemoteFiles.size} remote files")

        var matchCount = 0
        var missingCount = 0

        AvailableModels.ALL_MODELS.forEach { model ->
            val remoteMatch = RemoteModelSource.findRemoteMatch(model.fileName, allRemoteFiles)
            if (remoteMatch != null) {
                matchCount++
                Log.d(TAG, "  MATCH: ${model.displayName} -> ${remoteMatch.source.repoName}")
            } else {
                missingCount++
                Log.w(TAG, "  MISSING: ${model.displayName} (${model.fileName})")
            }
        }

        Log.d(TAG, "Results: $matchCount matched, $missingCount missing")

        // All models should have remote matches
        assertThat(missingCount)
            .withFailureMessage("$missingCount models don't have remote matches")
            .isEqualTo(0)
    }

    /**
     * Test remote file download URLs are valid.
     */
    @Test
    fun testRemoteDownloadUrls_areValid() = runBlocking {
        val results = RemoteModelSource.fetchAllRemoteModels()
        val allRemoteFiles = results.values
            .mapNotNull { it.getOrNull() }
            .flatten()

        // Check a sample of URLs (first 5 from each source)
        val sampleFiles = allRemoteFiles.groupBy { it.source }
            .flatMap { (_, files) -> files.take(5) }

        Log.d(TAG, "Checking ${sampleFiles.size} sample download URLs...")

        sampleFiles.forEach { file ->
            val url = java.net.URL(file.downloadUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 10_000
            connection.setRequestProperty("User-Agent", "Transcribro-Android-Test")

            try {
                val responseCode = connection.responseCode
                assertThat(responseCode)
                    .withFailureMessage("URL returned $responseCode: ${file.downloadUrl}")
                    .isEqualTo(200)

                val contentLength = connection.contentLengthLong
                if (contentLength > 0) {
                    // Verify size matches what API reported (within 10% tolerance)
                    val tolerance = file.sizeBytes * 0.1
                    assertThat(contentLength).isWithin(tolerance.toLong()).of(file.sizeBytes)
                }

                Log.d(TAG, "  OK: ${file.fileName}")
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * Test that remote files have correct source attribution.
     */
    @Test
    fun testRemoteFiles_haveCorrectSource() = runBlocking {
        ModelSource.entries.forEach { source ->
            val result = RemoteModelSource.fetchModelsFromSource(source)
            assertThat(result.isSuccess).isTrue()

            val files = result.getOrNull()!!
            files.forEach { file ->
                assertThat(file.source).isEqualTo(source)
                assertThat(file.downloadUrl).contains(source.repoOwner)
                assertThat(file.downloadUrl).contains(source.repoName)
            }
        }
    }
}
