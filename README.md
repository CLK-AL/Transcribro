# Transcribro

Transcribro is a private and on-device speech recognition keyboard and service for Android.\
It uses whisper.cpp to run the OpenAI Whisper family of models and Silero VAD for voice activity detection.\
It features a voice input keyboard, enabling you to type with speech.\
It can also be used by other apps either explicitly or when set as the user-selected speech to text app which some apps
may use for speech to text.

## Language support

Transcribro supports 99 languages through OpenAI Whisper multilingual models. Download models in-app based on your needs:

| Model | Size | Languages | Notes |
|-------|------|-----------|-------|
| Tiny Multilingual | ~26 MB | 99 languages | Default, fastest |
| Base/Small/Medium | 48-514 MB | 99 languages | Better accuracy |
| Large V3 Turbo | ~547 MB | 99 languages | Best speed/accuracy balance |
| Large V2/V3 | ~1 GB | 99 languages | Highest accuracy |
| English-only | 22-514 MB | English | Optimized for English |
| Hebrew (Ivrit AI) | 529 MB - 2.9 GB | Hebrew, English | Fine-tuned for Hebrew |

Models are downloaded from HuggingFace ([ggerganov/whisper.cpp](https://huggingface.co/ggerganov/whisper.cpp), [ivrit-ai](https://huggingface.co/ivrit-ai)).

## Model management

The Model Selection screen (Settings > Model Selection) allows you to:

- **Download models**: Tap checkbox to download a model from HuggingFace
- **Select active model**: Tap a downloaded model to use it for transcription
- **Check for updates**: Sync with remote sources to detect model updates
- **View disk usage**: See total size of downloaded models
- **Manage files**: View and delete orphaned files

### UI indicators

| Icon | Meaning |
|------|---------|
| ⬇️ | Available for download |
| ⏳ | Downloading |
| ✅ | Downloaded |
| ✓ | Currently selected |
| 🆙 | Update available |
| 🌍 | Multilingual model |
| 🇺🇸 | English-only model |
| 🇮🇱 | Hebrew model |
| 2️⃣-8️⃣ | Quantization level |
| 📄 | Known model file |
| 🔗 | Matches remote source |
| ⚠️ | Orphaned file |

### Architecture diagrams

PlantUML diagrams in [docs/puml/](docs/puml/):

| Diagram | Description |
|---------|-------------|
| [architecture.puml](docs/puml/architecture.puml) | App component architecture |
| [ui-navigation.puml](docs/puml/ui-navigation.puml) | Screen navigation flow |
| [recognition-flow.puml](docs/puml/recognition-flow.puml) | Speech recognition sequence |
| [model-download-flow.puml](docs/puml/model-download-flow.puml) | Model download sequence |
| [download-states.puml](docs/puml/download-states.puml) | Download state machine |
| [model-classes.puml](docs/puml/model-classes.puml) | Model manager classes |
| [preferences-flow.puml](docs/puml/preferences-flow.puml) | DataStore preferences flow |

## Future work

- **CTranslate2/Faster Whisper support**: Currently uses whisper.cpp with GGML format models. CTranslate2 (used by Faster Whisper) offers potentially faster inference but lacks Android NDK support as of 2026. If CTranslate2 adds Android support in the future, CT2 format models could be integrated as an alternative inference engine.

## Download

Transcribro is available on the [Accrescent](https://accrescent.app) app store and GitHub releases.\
[Accrescent](https://accrescent.app) is the recommended way to get Transcribro as it is more secure
than GitHub releases.\
Click on the badge below to get it on [Accrescent](https://accrescent.app).

<a href="https://accrescent.app/app/dev.soupslurpr.transcribro">
    <img alt="Get it on Accrescent" src="https://accrescent.app/badges/get-it-on.png" height="80">
</a>

The package name and SHA-256 hash of the signing certificate is below, so if you are downloading the APK, you can
verify Transcribro with [`apksigner`](https://developer.android.com/studio/command-line/apksigner#usage-verify)
using `apksigner verify --print-certs Transcribro-X.Y.Z.apk` and/or
[AppVerifier](https://github.com/soupslurpr/AppVerifier).
If you are downloading from [Accrescent](https://accrescent.app) then you should verify
[Accrescent](https://accrescent.app) itself [here](https://accrescent.app/faq#verifying).

dev.soupslurpr.transcribro\
7D:BC:FB:FA:A1:35:B4:4E:6E:93:91:02:25:DC:B1:4E:05:82:91:DA:8C:2D:36:22:73:49:49:B7:1A:B3:BE:64

It can also be found on a [Bluesky post](https://bsky.app/profile/soupslurpr.dev/post/3kopox4ffl72t)
to distrust the website.
It is encouraged to verify it's the same with other people as well for assurance.

## Community

Join the Matrix space at https://matrix.to/#/#transcribro:matrix.org for the General, Announcements, and
Testing rooms.

## Contributing

Check [CONTRIBUTING.md](https://github.com/soupslurpr/Transcribro/blob/master/CONTRIBUTING.md) for things to know
if you want to contribute.

## Donation

Thank you to everyone who donated.

## Screenshots

<img src="/Screenshot_20250214-223133.png" alt="Screenshot of the keyboard UI, focused on the search bar of Vanadium's incognito tab." width="250">
