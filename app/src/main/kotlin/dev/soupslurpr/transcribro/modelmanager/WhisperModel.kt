package dev.soupslurpr.transcribro.modelmanager

/**
 * Represents a Whisper model that can be downloaded and used for transcription.
 */
data class WhisperModel(
    val id: String,
    val displayName: String,
    val description: String,
    val languages: List<String>,
    val sizeBytes: Long,
    val downloadUrl: String,
    val fileName: String,
    val quantization: String? = null,
)

/**
 * Available Whisper models for download.
 */
object AvailableModels {

    private const val WHISPER_CPP_BASE_URL = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main"
    private const val IVRIT_GGML_BASE_URL = "https://huggingface.co/thewh1teagle/ivrit-ggml/resolve/main"
    private const val IVRIT_AI_V3_BASE_URL = "https://huggingface.co/ivrit-ai/whisper-large-v3-ggml/resolve/main"
    private const val IVRIT_AI_V3_TURBO_BASE_URL = "https://huggingface.co/ivrit-ai/whisper-large-v3-turbo-ggml/resolve/main"

    // ==================== Tiny Models ====================

    /**
     * Multilingual tiny model - supports 99 languages. DEFAULT MODEL.
     */
    val TINY_MULTILINGUAL = WhisperModel(
        id = "tiny-q5_0",
        displayName = "Tiny Multilingual",
        description = "Fastest multilingual. Supports 99 languages. Default.",
        languages = listOf("multilingual"),
        sizeBytes = 26_000_000L, // ~26 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-tiny-q5_0.bin",
        fileName = "ggml-tiny-q5_0.bin",
        quantization = "q5_0"
    )

    /**
     * English-only tiny model.
     */
    val TINY_EN = WhisperModel(
        id = "tiny.en-q5_0",
        displayName = "Tiny English",
        description = "Fastest English-only.",
        languages = listOf("en"),
        sizeBytes = 22_000_000L, // ~22 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-tiny.en-q5_0.bin",
        fileName = "ggml-tiny.en-q5_0.bin",
        quantization = "q5_0"
    )

    /**
     * Multilingual tiny model - higher quality.
     */
    val TINY_MULTILINGUAL_Q8 = WhisperModel(
        id = "tiny-q8_0",
        displayName = "Tiny Multilingual (Q8)",
        description = "Better quality tiny. 99 languages.",
        languages = listOf("multilingual"),
        sizeBytes = 42_000_000L, // ~42 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-tiny-q8_0.bin",
        fileName = "ggml-tiny-q8_0.bin",
        quantization = "q8_0"
    )

    // ==================== Base Models ====================

    /**
     * Multilingual base model - better accuracy than tiny.
     */
    val BASE_MULTILINGUAL = WhisperModel(
        id = "base-q5_0",
        displayName = "Base Multilingual",
        description = "Good balance of speed and accuracy. 99 languages.",
        languages = listOf("multilingual"),
        sizeBytes = 48_000_000L, // ~48 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-base-q5_0.bin",
        fileName = "ggml-base-q5_0.bin",
        quantization = "q5_0"
    )

    /**
     * English-only base model.
     */
    val BASE_EN = WhisperModel(
        id = "base.en-q5_0",
        displayName = "Base English",
        description = "Good accuracy for English. Fast processing.",
        languages = listOf("en"),
        sizeBytes = 42_000_000L, // ~42 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-base.en-q5_0.bin",
        fileName = "ggml-base.en-q5_0.bin",
        quantization = "q5_0"
    )

    // ==================== Small Models ====================

    /**
     * Multilingual small model - good accuracy.
     */
    val SMALL_MULTILINGUAL = WhisperModel(
        id = "small-q5_0",
        displayName = "Small Multilingual",
        description = "Good accuracy for most languages.",
        languages = listOf("multilingual"),
        sizeBytes = 182_000_000L, // ~182 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-small-q5_0.bin",
        fileName = "ggml-small-q5_0.bin",
        quantization = "q5_0"
    )

    /**
     * English-only small model.
     */
    val SMALL_EN = WhisperModel(
        id = "small.en-q5_0",
        displayName = "Small English",
        description = "Good accuracy for English.",
        languages = listOf("en"),
        sizeBytes = 182_000_000L, // ~182 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-small.en-q5_0.bin",
        fileName = "ggml-small.en-q5_0.bin",
        quantization = "q5_0"
    )

    /**
     * Multilingual small model - higher quality quantization.
     */
    val SMALL_MULTILINGUAL_Q8 = WhisperModel(
        id = "small-q8_0",
        displayName = "Small Multilingual (Q8)",
        description = "Better quality, larger file. 99 languages.",
        languages = listOf("multilingual"),
        sizeBytes = 256_000_000L, // ~256 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-small-q8_0.bin",
        fileName = "ggml-small-q8_0.bin",
        quantization = "q8_0"
    )

    // ==================== Medium Models ====================

    /**
     * Multilingual medium model - high accuracy.
     */
    val MEDIUM_MULTILINGUAL = WhisperModel(
        id = "medium-q5_0",
        displayName = "Medium Multilingual",
        description = "High accuracy, slower. 99 languages.",
        languages = listOf("multilingual"),
        sizeBytes = 514_000_000L, // ~514 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-medium-q5_0.bin",
        fileName = "ggml-medium-q5_0.bin",
        quantization = "q5_0"
    )

    /**
     * English-only medium model.
     */
    val MEDIUM_EN = WhisperModel(
        id = "medium.en-q5_0",
        displayName = "Medium English",
        description = "High accuracy for English.",
        languages = listOf("en"),
        sizeBytes = 514_000_000L, // ~514 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-medium.en-q5_0.bin",
        fileName = "ggml-medium.en-q5_0.bin",
        quantization = "q5_0"
    )

    // ==================== Large Models ====================

    /**
     * Large V2 model - best accuracy for most languages.
     */
    val LARGE_V2 = WhisperModel(
        id = "large-v2-q5_0",
        displayName = "Large V2",
        description = "Best multilingual accuracy. Very large.",
        languages = listOf("multilingual"),
        sizeBytes = 1_080_000_000L, // ~1.08 GB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-large-v2-q5_0.bin",
        fileName = "ggml-large-v2-q5_0.bin",
        quantization = "q5_0"
    )

    /**
     * Large V3 model - latest large model.
     */
    val LARGE_V3 = WhisperModel(
        id = "large-v3-q5_0",
        displayName = "Large V3",
        description = "Latest large model. Highest accuracy.",
        languages = listOf("multilingual"),
        sizeBytes = 1_080_000_000L, // ~1.08 GB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-large-v3-q5_0.bin",
        fileName = "ggml-large-v3-q5_0.bin",
        quantization = "q5_0"
    )

    /**
     * Large V3 Turbo - optimized for speed while maintaining quality.
     */
    val LARGE_V3_TURBO = WhisperModel(
        id = "large-v3-turbo-q5_0",
        displayName = "Large V3 Turbo",
        description = "Fast large model. Good accuracy, faster than V3.",
        languages = listOf("multilingual"),
        sizeBytes = 547_000_000L, // ~547 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-large-v3-turbo-q5_0.bin",
        fileName = "ggml-large-v3-turbo-q5_0.bin",
        quantization = "q5_0"
    )

    /**
     * Large V3 Turbo - higher quality quantization.
     */
    val LARGE_V3_TURBO_Q8 = WhisperModel(
        id = "large-v3-turbo-q8_0",
        displayName = "Large V3 Turbo (Q8)",
        description = "Better quality turbo model.",
        languages = listOf("multilingual"),
        sizeBytes = 857_000_000L, // ~857 MB
        downloadUrl = "$WHISPER_CPP_BASE_URL/ggml-large-v3-turbo-q8_0.bin",
        fileName = "ggml-large-v3-turbo-q8_0.bin",
        quantization = "q8_0"
    )

    // ==================== Hebrew Models ====================

    /**
     * Hebrew-optimized model from Ivrit AI (smallest quantization).
     */
    val HEBREW_IVRIT_Q2K = WhisperModel(
        id = "ivrit-v2-d4-q2_k",
        displayName = "Hebrew (Ivrit AI)",
        description = "Optimized for Hebrew. Best Hebrew accuracy.",
        languages = listOf("he", "en"),
        sizeBytes = 529_000_000L, // ~529 MB
        downloadUrl = "$IVRIT_GGML_BASE_URL/ggml-ivrit-v2-d4-q2_k.bin",
        fileName = "ggml-ivrit-v2-d4-q2_k.bin",
        quantization = "q2_k"
    )

    /**
     * Hebrew-optimized model from Ivrit AI (better quality).
     */
    val HEBREW_IVRIT_Q4K = WhisperModel(
        id = "ivrit-v2-d4-q4_k",
        displayName = "Hebrew (Ivrit AI) HQ",
        description = "High quality Hebrew transcription.",
        languages = listOf("he", "en"),
        sizeBytes = 889_000_000L, // ~889 MB
        downloadUrl = "$IVRIT_GGML_BASE_URL/ggml-ivrit-v2-d4-q4_k.bin",
        fileName = "ggml-ivrit-v2-d4-q4_k.bin",
        quantization = "q4_k"
    )

    /**
     * Hebrew Large V3 model from Ivrit AI (full precision GGML).
     * Based on whisper-large-v3 fine-tuned for Hebrew.
     */
    val HEBREW_IVRIT_V3 = WhisperModel(
        id = "ivrit-v3-large",
        displayName = "Hebrew V3 Large",
        description = "Large V3 fine-tuned for Hebrew. Best Hebrew quality.",
        languages = listOf("he", "en"),
        sizeBytes = 3_095_033_483L, // ~2.88 GB
        downloadUrl = "$IVRIT_AI_V3_BASE_URL/ggml-model.bin",
        fileName = "ggml-ivrit-v3-large.bin",
        quantization = null
    )

    /**
     * Hebrew Large V3 Turbo model from Ivrit AI (GGML).
     * Faster version of Large V3 fine-tuned for Hebrew.
     */
    val HEBREW_IVRIT_V3_TURBO = WhisperModel(
        id = "ivrit-v3-large-turbo",
        displayName = "Hebrew V3 Large Turbo",
        description = "Fast large model fine-tuned for Hebrew.",
        languages = listOf("he", "en"),
        sizeBytes = 1_624_555_275L, // ~1.5 GB
        downloadUrl = "$IVRIT_AI_V3_TURBO_BASE_URL/ggml-model.bin",
        fileName = "ggml-ivrit-v3-large-turbo.bin",
        quantization = null
    )

    /**
     * All available models grouped by category.
     */
    val ALL_MODELS = listOf(
        // Tiny (default first)
        TINY_MULTILINGUAL,
        TINY_EN,
        TINY_MULTILINGUAL_Q8,
        // Base
        BASE_EN,
        BASE_MULTILINGUAL,
        // Small
        SMALL_EN,
        SMALL_MULTILINGUAL,
        SMALL_MULTILINGUAL_Q8,
        // Medium
        MEDIUM_EN,
        MEDIUM_MULTILINGUAL,
        // Large
        LARGE_V3_TURBO,
        LARGE_V3_TURBO_Q8,
        LARGE_V2,
        LARGE_V3,
        // Hebrew
        HEBREW_IVRIT_Q2K,
        HEBREW_IVRIT_Q4K,
        HEBREW_IVRIT_V3_TURBO,
        HEBREW_IVRIT_V3,
    )

    /**
     * Get a model by its ID.
     */
    fun getById(id: String): WhisperModel? = ALL_MODELS.find { it.id == id }

    /**
     * Get the default model (Tiny Multilingual).
     */
    fun getDefault(): WhisperModel = TINY_MULTILINGUAL

    /**
     * Format file size for display.
     */
    fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> String.format("%.1f GB", bytes / 1_000_000_000.0)
            bytes >= 1_000_000 -> String.format("%.0f MB", bytes / 1_000_000.0)
            bytes >= 1_000 -> String.format("%.0f KB", bytes / 1_000.0)
            else -> "$bytes B"
        }
    }
}
