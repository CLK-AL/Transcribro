package dev.soupslurpr.transcribro.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
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

            ModelRow(
                model = model,
                isSelected = isSelected,
                isDownloaded = isDownloaded,
                downloadState = downloadState,
                onCheckedChange = { checked ->
                    if (checked) {
                        if (isDownloaded) {
                            // Select model
                            preferencesViewModel.setPreference(
                                preferencesUiState.selectedModelId.first,
                                model.id
                            )
                        } else if (!model.isBuiltIn) {
                            // Download model
                            coroutineScope.launch {
                                modelDownloadManager.downloadModel(model)
                            }
                        }
                    } else {
                        if (!model.isBuiltIn && isDownloaded) {
                            // Delete model
                            modelDownloadManager.deleteModel(model)
                            // If deleted model was selected, switch to default
                            if (isSelected) {
                                preferencesViewModel.setPreference(
                                    preferencesUiState.selectedModelId.first,
                                    AvailableModels.getDefault().id
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Get emoji flag for language.
 */
private fun getLanguageEmoji(languages: List<String>): String {
    return when {
        languages.contains("he") -> "\uD83C\uDDEE\uD83C\uDDF1" // 🇮🇱
        languages.contains("multilingual") -> "\uD83C\uDF0D" // 🌍
        languages.contains("en") -> "\uD83C\uDDFA\uD83C\uDDF8" // 🇺🇸
        else -> "\uD83C\uDF10" // 🌐
    }
}

/**
 * Get emoji number for quantization level.
 */
private fun getQuantizationEmoji(quantization: String?): String {
    return when {
        quantization == null -> ""
        quantization.contains("2") -> "2\uFE0F\u20E3" // 2️⃣
        quantization.contains("3") -> "3\uFE0F\u20E3" // 3️⃣
        quantization.contains("4") -> "4\uFE0F\u20E3" // 4️⃣
        quantization.contains("5") -> "5\uFE0F\u20E3" // 5️⃣
        quantization.contains("6") -> "6\uFE0F\u20E3" // 6️⃣
        quantization.contains("8") -> "8\uFE0F\u20E3" // 8️⃣
        else -> ""
    }
}

@Composable
fun ModelRow(
    model: WhisperModel,
    isSelected: Boolean,
    isDownloaded: Boolean,
    downloadState: DownloadState,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDownloading = downloadState is DownloadState.Downloading
    val languageEmoji = getLanguageEmoji(model.languages)
    val quantEmoji = getQuantizationEmoji(model.quantization)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDownloading) {
                if (isDownloaded) {
                    onCheckedChange(true) // Select
                } else if (!model.isBuiltIn) {
                    onCheckedChange(true) // Download
                }
            }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isDownloaded,
                onCheckedChange = { checked ->
                    if (!isDownloading) {
                        onCheckedChange(checked)
                    }
                },
                enabled = !isDownloading && !model.isBuiltIn
            )

            // Model info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$languageEmoji $quantEmoji",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = model.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Text(
                            text = "\u2713", // ✓
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "${model.description} (${AvailableModels.formatSize(model.sizeBytes)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status indicator
            when {
                model.isBuiltIn -> {
                    Text(
                        text = "\uD83D\uDCE6", // 📦
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                isDownloading -> {
                    Text(
                        text = "\u23F3", // ⏳
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                isDownloaded -> {
                    Text(
                        text = "\u2705", // ✅
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    Text(
                        text = "\u2B07\uFE0F", // ⬇️
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Download progress
        if (downloadState is DownloadState.Downloading) {
            Column(modifier = Modifier.padding(start = 48.dp, top = 4.dp, end = 16.dp)) {
                LinearProgressIndicator(
                    progress = { downloadState.progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${(downloadState.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Error state
        if (downloadState is DownloadState.Error) {
            Text(
                text = "\u274C ${downloadState.message}", // ❌
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 48.dp, top = 4.dp)
            )
        }
    }
}
