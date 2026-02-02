package dev.soupslurpr.transcribro.modelmanager

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for RemoteModelSource and related classes.
 */
class RemoteModelSourceTest {

    @Test
    fun `ModelSource has correct repo info for WHISPER_CPP`() {
        val source = ModelSource.WHISPER_CPP

        assertThat(source.repoOwner).isEqualTo("ggerganov")
        assertThat(source.repoName).isEqualTo("whisper.cpp")
        assertThat(source.branch).isEqualTo("main")
    }

    @Test
    fun `ModelSource has correct repo info for IVRIT_GGML`() {
        val source = ModelSource.IVRIT_GGML

        assertThat(source.repoOwner).isEqualTo("thewh1teagle")
        assertThat(source.repoName).isEqualTo("ivrit-ggml")
        assertThat(source.branch).isEqualTo("main")
    }

    @Test
    fun `ModelSource apiUrl is correctly formatted`() {
        val whisperUrl = ModelSource.WHISPER_CPP.apiUrl
        val ivritUrl = ModelSource.IVRIT_GGML.apiUrl

        assertThat(whisperUrl).isEqualTo("https://huggingface.co/api/models/ggerganov/whisper.cpp/tree/main")
        assertThat(ivritUrl).isEqualTo("https://huggingface.co/api/models/thewh1teagle/ivrit-ggml/tree/main")
    }

    @Test
    fun `ModelSource downloadBaseUrl is correctly formatted`() {
        val whisperUrl = ModelSource.WHISPER_CPP.downloadBaseUrl
        val ivritUrl = ModelSource.IVRIT_GGML.downloadBaseUrl

        assertThat(whisperUrl).isEqualTo("https://huggingface.co/ggerganov/whisper.cpp/resolve/main")
        assertThat(ivritUrl).isEqualTo("https://huggingface.co/thewh1teagle/ivrit-ggml/resolve/main")
    }

    @Test
    fun `RemoteFile has correct properties`() {
        val file = RemoteFile(
            fileName = "ggml-tiny.bin",
            path = "ggml-tiny.bin",
            sizeBytes = 26_000_000L,
            downloadUrl = "https://example.com/ggml-tiny.bin",
            source = ModelSource.WHISPER_CPP
        )

        assertThat(file.fileName).isEqualTo("ggml-tiny.bin")
        assertThat(file.path).isEqualTo("ggml-tiny.bin")
        assertThat(file.sizeBytes).isEqualTo(26_000_000L)
        assertThat(file.downloadUrl).isEqualTo("https://example.com/ggml-tiny.bin")
        assertThat(file.source).isEqualTo(ModelSource.WHISPER_CPP)
    }

    @Test
    fun `findRemoteMatch returns matching file`() {
        val remoteFiles = listOf(
            RemoteFile("ggml-tiny-q5_0.bin", "ggml-tiny-q5_0.bin", 26_000_000L,
                "https://example.com/ggml-tiny-q5_0.bin", ModelSource.WHISPER_CPP),
            RemoteFile("ggml-base-q5_0.bin", "ggml-base-q5_0.bin", 48_000_000L,
                "https://example.com/ggml-base-q5_0.bin", ModelSource.WHISPER_CPP)
        )

        val match = RemoteModelSource.findRemoteMatch("ggml-tiny-q5_0.bin", remoteFiles)

        assertThat(match).isNotNull()
        assertThat(match!!.fileName).isEqualTo("ggml-tiny-q5_0.bin")
    }

    @Test
    fun `findRemoteMatch returns null for no match`() {
        val remoteFiles = listOf(
            RemoteFile("ggml-tiny-q5_0.bin", "ggml-tiny-q5_0.bin", 26_000_000L,
                "https://example.com/ggml-tiny-q5_0.bin", ModelSource.WHISPER_CPP)
        )

        val match = RemoteModelSource.findRemoteMatch("nonexistent.bin", remoteFiles)

        assertThat(match).isNull()
    }

    @Test
    fun `findRemoteMatch returns null for empty list`() {
        val match = RemoteModelSource.findRemoteMatch("any.bin", emptyList())

        assertThat(match).isNull()
    }

    @Test
    fun `needsUpdate returns true when sizes differ significantly`() {
        val remoteFile = RemoteFile("model.bin", "model.bin", 26_000_000L,
            "https://example.com/model.bin", ModelSource.WHISPER_CPP)

        // Size differs by more than 1KB
        assertThat(RemoteModelSource.needsUpdate(25_000_000L, remoteFile)).isTrue()
        assertThat(RemoteModelSource.needsUpdate(27_000_000L, remoteFile)).isTrue()
    }

    @Test
    fun `needsUpdate returns false when sizes are similar`() {
        val remoteFile = RemoteFile("model.bin", "model.bin", 26_000_000L,
            "https://example.com/model.bin", ModelSource.WHISPER_CPP)

        // Size differs by less than 1KB
        assertThat(RemoteModelSource.needsUpdate(26_000_000L, remoteFile)).isFalse()
        assertThat(RemoteModelSource.needsUpdate(26_000_500L, remoteFile)).isFalse()
        assertThat(RemoteModelSource.needsUpdate(25_999_500L, remoteFile)).isFalse()
    }

    @Test
    fun `needsUpdate handles exact match`() {
        val remoteFile = RemoteFile("model.bin", "model.bin", 26_000_000L,
            "https://example.com/model.bin", ModelSource.WHISPER_CPP)

        assertThat(RemoteModelSource.needsUpdate(26_000_000L, remoteFile)).isFalse()
    }

    @Test
    fun `ModelSource entries returns all sources`() {
        val sources = ModelSource.entries

        assertThat(sources).hasSize(2)
        assertThat(sources).contains(ModelSource.WHISPER_CPP)
        assertThat(sources).contains(ModelSource.IVRIT_GGML)
    }
}
