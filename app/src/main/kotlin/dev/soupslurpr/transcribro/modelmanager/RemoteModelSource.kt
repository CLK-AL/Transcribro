package dev.soupslurpr.transcribro.modelmanager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Represents a remote file from a model provider.
 */
data class RemoteFile(
    val fileName: String,
    val path: String,
    val sizeBytes: Long,
    val downloadUrl: String,
    val source: ModelSource
)

/**
 * Known model sources with their HuggingFace repo info.
 */
enum class ModelSource(
    val repoOwner: String,
    val repoName: String,
    val branch: String = "main"
) {
    WHISPER_CPP("ggerganov", "whisper.cpp"),
    IVRIT_GGML("thewh1teagle", "ivrit-ggml");

    val apiUrl: String
        get() = "https://huggingface.co/api/models/$repoOwner/$repoName/tree/$branch"

    val downloadBaseUrl: String
        get() = "https://huggingface.co/$repoOwner/$repoName/resolve/$branch"
}

/**
 * Fetches model lists from remote sources (HuggingFace).
 */
object RemoteModelSource {

    /**
     * Fetch list of .bin model files from a HuggingFace repo.
     */
    suspend fun fetchModelsFromSource(source: ModelSource): Result<List<RemoteFile>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(source.apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("User-Agent", "Transcribro-Android")

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("HTTP $responseCode"))
            }

            val response = connection.inputStream.bufferedReader().readText()
            connection.disconnect()

            val files = parseHuggingFaceResponse(response, source)
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch models from all known sources.
     */
    suspend fun fetchAllRemoteModels(): Map<ModelSource, Result<List<RemoteFile>>> {
        val results = mutableMapOf<ModelSource, Result<List<RemoteFile>>>()
        ModelSource.entries.forEach { source ->
            results[source] = fetchModelsFromSource(source)
        }
        return results
    }

    /**
     * Parse HuggingFace API response to extract .bin files.
     */
    private fun parseHuggingFaceResponse(json: String, source: ModelSource): List<RemoteFile> {
        val files = mutableListOf<RemoteFile>()
        val jsonArray = JSONArray(json)

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val type = item.getString("type")
            val path = item.getString("path")

            if (type == "file" && path.endsWith(".bin")) {
                val size = item.optLong("size", 0L)
                val fileName = path.substringAfterLast("/")

                files.add(
                    RemoteFile(
                        fileName = fileName,
                        path = path,
                        sizeBytes = size,
                        downloadUrl = "${source.downloadBaseUrl}/$path",
                        source = source
                    )
                )
            }
        }

        return files.sortedBy { it.fileName }
    }

    /**
     * Find matching remote file for a local file by name.
     */
    fun findRemoteMatch(
        localFileName: String,
        remoteFiles: List<RemoteFile>
    ): RemoteFile? {
        return remoteFiles.find { it.fileName == localFileName }
    }

    /**
     * Compare local file with remote to check if update is needed.
     */
    fun needsUpdate(localSizeBytes: Long, remoteFile: RemoteFile): Boolean {
        // If sizes differ by more than 1KB, consider it an update
        return kotlin.math.abs(remoteFile.sizeBytes - localSizeBytes) > 1024
    }
}
