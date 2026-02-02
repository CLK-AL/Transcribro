package dev.soupslurpr.transcribro.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.soupslurpr.transcribro.R
import dev.soupslurpr.transcribro.dataStore
import dev.soupslurpr.transcribro.modelmanager.AvailableModels
import dev.soupslurpr.transcribro.modelmanager.DownloadState
import dev.soupslurpr.transcribro.modelmanager.ModelDownloadManager
import dev.soupslurpr.transcribro.modelmanager.WhisperModel
import dev.soupslurpr.transcribro.preferences.PreferencesViewModel
import dev.soupslurpr.transcribro.ui.reusablecomposables.ScreenLazyColumn
import kotlinx.coroutines.launch

@Composable
fun ModelSelectionScreen() {
    val context = LocalContext.current
    val preferencesViewModel: PreferencesViewModel = viewModel(
        factory = PreferencesViewModel.PreferencesViewModelFactory(context.dataStore)
    )
    val preferencesUiState by preferencesViewModel.uiState.collectAsState()
    val selectedModelId = preferencesUiState.selectedModelId.second.value

    val modelDownloadManager = remember { ModelDownloadManager(context) }
    val downloadStates by modelDownloadManager.downloadStates.collectAsState(initial = emptyMap())
    val coroutineScope = rememberCoroutineScope()

    ScreenLazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = stringResource(R.string.model_selection_setting_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(AvailableModels.ALL_MODELS.size) { index ->
            val model = AvailableModels.ALL_MODELS[index]
            val isSelected = model.id == selectedModelId
            val isDownloaded = modelDownloadManager.isModelDownloaded(model)
            val downloadState = downloadStates[model.id] ?: DownloadState.Idle

            ModelCard(
                model = model,
                isSelected = isSelected,
                isDownloaded = isDownloaded,
                downloadState = downloadState,
                onSelect = {
                    if (isDownloaded) {
                        preferencesViewModel.setPreference(
                            preferencesUiState.selectedModelId.first,
                            model.id
                        )
                    }
                },
                onDownload = {
                    coroutineScope.launch {
                        modelDownloadManager.downloadModel(model)
                    }
                },
                onDelete = {
                    modelDownloadManager.deleteModel(model)
                    // If deleted model was selected, switch to default
                    if (isSelected) {
                        preferencesViewModel.setPreference(
                            preferencesUiState.selectedModelId.first,
                            AvailableModels.getDefault().id
                        )
                    }
                },
                onCancelDownload = {
                    modelDownloadManager.cancelDownload(model)
                }
            )
        }
    }
}

@Composable
fun ModelCard(
    model: WhisperModel,
    isSelected: Boolean,
    isDownloaded: Boolean,
    downloadState: DownloadState,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    onCancelDownload: () -> Unit
) {
    val isDownloading = downloadState is DownloadState.Downloading

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isDownloaded && !isDownloading) { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = model.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.model_selected),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = model.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = AvailableModels.formatSize(model.sizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action buttons
                when {
                    model.isBuiltIn -> {
                        Text(
                            text = stringResource(R.string.model_built_in),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    isDownloading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    isDownloaded -> {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.model_delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {
                        IconButton(onClick = onDownload) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = stringResource(R.string.model_download),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Download progress
            if (downloadState is DownloadState.Downloading) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    LinearProgressIndicator(
                        progress = { downloadState.progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${stringResource(R.string.model_downloading)} ${(downloadState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Error state
            if (downloadState is DownloadState.Error) {
                Text(
                    text = "${stringResource(R.string.model_download_error)}: ${downloadState.message}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Status text
            if (!model.isBuiltIn && downloadState !is DownloadState.Downloading && downloadState !is DownloadState.Error) {
                Text(
                    text = if (isDownloaded) {
                        stringResource(R.string.model_downloaded)
                    } else {
                        stringResource(R.string.model_not_downloaded)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDownloaded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
