package dev.soupslurpr.transcribro.modelmanager

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Download state for a model.
 */
sealed class DownloadState {
    data object Idle : DownloadState()
    data class Downloading(val progress: Float, val bytesDownloaded: Long, val totalBytes: Long) : DownloadState()
    data object Completed : DownloadState()
    data class Error(val message: String) : DownloadState()
}

/**
 * Manages downloading, storing, and accessing Whisper models.
 */
class ModelDownloadManager(private val context: Context) {

    private val modelsDir: File by lazy {
        File(context.filesDir, "whisper_models").also { it.mkdirs() }
    }

    private val _downloadStates = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val downloadStates: Flow<Map<String, DownloadState>> = _downloadStates.asStateFlow()

    /**
     * Check if a model is downloaded and available.
     */
    fun isModelDownloaded(model: WhisperModel): Boolean {
        if (model.isBuiltIn) return true
        val modelFile = File(modelsDir, model.fileName)
        return modelFile.exists() && modelFile.length() > 0
    }

    /**
     * Get the file path for a model.
     * Returns null if the model is not downloaded.
     */
    fun getModelPath(model: WhisperModel): String? {
        if (model.isBuiltIn) {
            return null // Built-in models are loaded from assets
        }
        val modelFile = File(modelsDir, model.fileName)
        return if (modelFile.exists()) modelFile.absolutePath else null
    }

    /**
     * Get all downloaded models.
     */
    fun getDownloadedModels(): List<WhisperModel> {
        return AvailableModels.ALL_MODELS.filter { isModelDownloaded(it) }
    }

    /**
     * Get the size of downloaded models on disk.
     */
    fun getDownloadedModelsSize(): Long {
        return modelsDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * Download a model.
     */
    suspend fun downloadModel(model: WhisperModel): Result<File> = withContext(Dispatchers.IO) {
        if (model.isBuiltIn) {
            return@withContext Result.failure(IllegalArgumentException("Cannot download built-in model"))
        }

        val modelFile = File(modelsDir, model.fileName)
        val tempFile = File(modelsDir, "${model.fileName}.tmp")

        try {
            updateDownloadState(model.id, DownloadState.Downloading(0f, 0, model.sizeBytes))

            val url = URL(model.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 30_000
            connection.readTimeout = 60_000
            connection.setRequestProperty("User-Agent", "Transcribro-Android")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("Server returned HTTP $responseCode")
            }

            val totalBytes = connection.contentLengthLong.takeIf { it > 0 } ?: model.sizeBytes
            var bytesDownloaded = 0L

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead

                        val progress = (bytesDownloaded.toFloat() / totalBytes).coerceIn(0f, 1f)
                        updateDownloadState(
                            model.id,
                            DownloadState.Downloading(progress, bytesDownloaded, totalBytes)
                        )
                    }
                }
            }

            // Rename temp file to final file
            if (modelFile.exists()) {
                modelFile.delete()
            }
            tempFile.renameTo(modelFile)

            updateDownloadState(model.id, DownloadState.Completed)
            Result.success(modelFile)

        } catch (e: Exception) {
            tempFile.delete()
            updateDownloadState(model.id, DownloadState.Error(e.message ?: "Unknown error"))
            Result.failure(e)
        }
    }

    /**
     * Delete a downloaded model.
     */
    fun deleteModel(model: WhisperModel): Boolean {
        if (model.isBuiltIn) return false

        val modelFile = File(modelsDir, model.fileName)
        val deleted = modelFile.delete()

        if (deleted) {
            updateDownloadState(model.id, DownloadState.Idle)
        }

        return deleted
    }

    /**
     * Cancel an ongoing download.
     */
    fun cancelDownload(model: WhisperModel) {
        val tempFile = File(modelsDir, "${model.fileName}.tmp")
        tempFile.delete()
        updateDownloadState(model.id, DownloadState.Idle)
    }

    /**
     * Reset download state for a model.
     */
    fun resetDownloadState(modelId: String) {
        updateDownloadState(modelId, DownloadState.Idle)
    }

    private fun updateDownloadState(modelId: String, state: DownloadState) {
        _downloadStates.value = _downloadStates.value.toMutableMap().apply {
            this[modelId] = state
        }
    }
}
