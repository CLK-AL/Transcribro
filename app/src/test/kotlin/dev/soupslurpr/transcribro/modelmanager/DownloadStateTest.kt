package dev.soupslurpr.transcribro.modelmanager

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for DownloadState and DownloadedFile.
 */
class DownloadStateTest {

    @Test
    fun `DownloadState Idle is singleton`() {
        val idle1 = DownloadState.Idle
        val idle2 = DownloadState.Idle

        assertThat(idle1).isSameInstanceAs(idle2)
    }

    @Test
    fun `DownloadState Completed is singleton`() {
        val completed1 = DownloadState.Completed
        val completed2 = DownloadState.Completed

        assertThat(completed1).isSameInstanceAs(completed2)
    }

    @Test
    fun `DownloadState UpdateAvailable is singleton`() {
        val update1 = DownloadState.UpdateAvailable
        val update2 = DownloadState.UpdateAvailable

        assertThat(update1).isSameInstanceAs(update2)
    }

    @Test
    fun `DownloadState Checking is singleton`() {
        val checking1 = DownloadState.Checking
        val checking2 = DownloadState.Checking

        assertThat(checking1).isSameInstanceAs(checking2)
    }

    @Test
    fun `DownloadState Downloading has correct properties`() {
        val downloading = DownloadState.Downloading(
            progress = 0.5f,
            bytesDownloaded = 50_000_000L,
            totalBytes = 100_000_000L
        )

        assertThat(downloading.progress).isEqualTo(0.5f)
        assertThat(downloading.bytesDownloaded).isEqualTo(50_000_000L)
        assertThat(downloading.totalBytes).isEqualTo(100_000_000L)
    }

    @Test
    fun `DownloadState Error has message`() {
        val error = DownloadState.Error("Network error")

        assertThat(error.message).isEqualTo("Network error")
    }

    @Test
    fun `DownloadState instances are DownloadState type`() {
        assertThat(DownloadState.Idle).isInstanceOf(DownloadState::class.java)
        assertThat(DownloadState.Completed).isInstanceOf(DownloadState::class.java)
        assertThat(DownloadState.UpdateAvailable).isInstanceOf(DownloadState::class.java)
        assertThat(DownloadState.Checking).isInstanceOf(DownloadState::class.java)
        assertThat(DownloadState.Downloading(0f, 0L, 0L)).isInstanceOf(DownloadState::class.java)
        assertThat(DownloadState.Error("")).isInstanceOf(DownloadState::class.java)
    }

    @Test
    fun `DownloadedFile with matched model`() {
        val model = AvailableModels.getDefault()
        val file = DownloadedFile(
            fileName = model.fileName,
            sizeBytes = model.sizeBytes,
            matchedModel = model,
            matchedRemoteFile = null
        )

        assertThat(file.fileName).isEqualTo(model.fileName)
        assertThat(file.sizeBytes).isEqualTo(model.sizeBytes)
        assertThat(file.matchedModel).isEqualTo(model)
        assertThat(file.matchedRemoteFile).isNull()
    }

    @Test
    fun `DownloadedFile with matched remote file`() {
        val remoteFile = RemoteFile(
            fileName = "some-model.bin",
            path = "some-model.bin",
            sizeBytes = 50_000_000L,
            downloadUrl = "https://example.com/some-model.bin",
            source = ModelSource.WHISPER_CPP
        )

        val file = DownloadedFile(
            fileName = "some-model.bin",
            sizeBytes = 50_000_000L,
            matchedModel = null,
            matchedRemoteFile = remoteFile
        )

        assertThat(file.matchedModel).isNull()
        assertThat(file.matchedRemoteFile).isEqualTo(remoteFile)
    }

    @Test
    fun `DownloadedFile orphaned has no matches`() {
        val file = DownloadedFile(
            fileName = "unknown.bin",
            sizeBytes = 10_000_000L,
            matchedModel = null,
            matchedRemoteFile = null
        )

        assertThat(file.matchedModel).isNull()
        assertThat(file.matchedRemoteFile).isNull()
    }

    @Test
    fun `DownloadedFile matchedRemoteFile defaults to null`() {
        val file = DownloadedFile(
            fileName = "test.bin",
            sizeBytes = 1000L,
            matchedModel = null
        )

        assertThat(file.matchedRemoteFile).isNull()
    }

    @Test
    fun `DownloadState Downloading equality`() {
        val d1 = DownloadState.Downloading(0.5f, 50L, 100L)
        val d2 = DownloadState.Downloading(0.5f, 50L, 100L)
        val d3 = DownloadState.Downloading(0.6f, 50L, 100L)

        assertThat(d1).isEqualTo(d2)
        assertThat(d1).isNotEqualTo(d3)
    }

    @Test
    fun `DownloadState Error equality`() {
        val e1 = DownloadState.Error("error")
        val e2 = DownloadState.Error("error")
        val e3 = DownloadState.Error("different")

        assertThat(e1).isEqualTo(e2)
        assertThat(e1).isNotEqualTo(e3)
    }

    @Test
    fun `DownloadedFile equality`() {
        val model = AvailableModels.getDefault()
        val f1 = DownloadedFile("test.bin", 1000L, model, null)
        val f2 = DownloadedFile("test.bin", 1000L, model, null)
        val f3 = DownloadedFile("other.bin", 1000L, model, null)

        assertThat(f1).isEqualTo(f2)
        assertThat(f1).isNotEqualTo(f3)
    }
}
