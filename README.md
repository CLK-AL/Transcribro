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

| Diagram | Source | View |
|---------|--------|------|
| App architecture | [architecture.puml](docs/puml/architecture.puml) | [SVG](https://kroki.io/plantuml/svg/eNptVMGO2jAQvecrLE7bA_QPqkWKEKzYVUW67aHdw-DMBotgR7bDilb9907GDnagXDIz73nmTfzCo_NgfX9qBVh5UB6l7y0WXvkWxTcL2kmr9taIZQ4X7qh0BxZOQppTZzRqX_kLHbHEAN20xOlAHqFBMXvdiC1c0M7En0LQ72c1zKykRdRvApzgPELovdKNu6PEemA9mxrbClsapozOaAy8bgJryegOpWm0-o1VhygPeU8uEPtvJpY7iGfQlCXFXC3Nh24N1BHkHtdaYwNzhyfjMQg0vZXItFANhcD7cVCuQ8vEJD3uN3alt-BDg3LL8URq3GxYkt6PPSuJSTAoneERjlpkTCdKdtgZp7yxF2Zl1QltayS0JXjItktpvETVojXflyWj9JzIfgGvzhgsIR6eXjafrqo_woyF7LpcRAC3ah_zxdEznCqTARUtQcHYtSZ1e3AEsE4C8Zd--GrxHS1qiY7mU7Oh4G4OhDsRpbJ0YrFXWrzTavEAg6shH6bL1vS1mK37piGfrmC4CyKtV-NuDVmmAW3On_MtA6bOVvk5KPp86BLehn78AYj5_Euy_hhwdXR6fHItN2MWMzZxYJ4wul7dHcjW-y90Xx49WowfVhx8tVsKGcktdgMNnsng0D65LIWMZD5IYT7irle23M3kYIPJa42lR9Q1_VEW_wBXGr1g) |
| Screen navigation | [ui-navigation.puml](docs/puml/ui-navigation.puml) | [SVG](https://kroki.io/plantuml/svg/eNptVE1v2zAMvetXED0OMHb3YWi2tkDQZuvidZdhB0VmbCGKJEh02nTof58sf8lxfDIfH8UniuStJ-6oOSpoZKb5SVacpNGMJCmElzV8nzAWuIRwU7QhhXCI-ga4h2jDPwbhi_85bLjUgJrcGayRmlJfBgVSY0FqT64R7dF-7r_XfBeyH_C8M9yV7GNKjURSV34hoccHFb2Zw8pa8L114cpgY0pUAVEYVSz83xyWkpZxT1Kg9rjAn508cXGGZ6OkOCeyY6IxT6I7cfTS-4jViUsVqxApfmIPd2y_DsihqM2rB64UlOZVK8PLGHqM7gU7XKxGcdiZNyAzRnwOTiS8wn7iump4hbBXvLp23M-Ga5LvsUtA4QnVFVIh3zG8-d5E30d62Qep0EOIvZP-EG_aIdNFo52Hwnvyo2IsYd_il6wMHnWgXF5_9G7xaELaIydRX4v-4WzN9ez0mdxV17NRaP-fSO2RscywNw5ebBlC_TXSXSz7lPQhSTp1UBewRWEqHQpZWERRpwMQgaH9o5HDbxMaNdTcNhRGeeYKzS-FM7Y2GmHXEA3tP_p_Oa69cNLGVy2lt4qnPf3YTyesN_dRwgh0IgZzLmOc6Tklg7UmrFw4uYRXSTVwa32b7c-nv5BlX7rlwLpVE-1h4vNhRSEbsZaQjla-GPUZtTs1h69cHBhLAy8yRUI_ha2re6uuWVu7f1aW6Byrkg9rLdSLjWhM0L3drFCsBy-O2IbN6TQQvhFjt6jLsLnZf_2y004=) |
| Speech recognition | [recognition-flow.puml](docs/puml/recognition-flow.puml) | [SVG](https://kroki.io/plantuml/svg/eNqVlE1vGjEQhu_-FaOcFimkTQ89oFKFQqPSJggBbQ9VD87uLFgY27K9JOTXZ2wvsAuoUleC_fBj-52Z13PnPLe-2kiwmOulEl5o1S2lfmZeeIkwN4j5CmbHUbgPo4znXlv46dAyQ0uIXBiuPFz9wN2T5rZ4NzDmCrgDureJRy5UY7052q3IMbL1c5ufC4lW_xqMIkL39vDvlXAG7QyNdoI07SIWXi9yQ608vvgI1c9t7jlxN7kxkH2fjDsRnXAvtshYvw_joJtL8cpjOvp9RiFC9_NefQ9iUh-E86iEWmYdVo8EKAjrwRJ9Ww9BYSQQ9ZcePGhewEYXKKG0egPGYokWVY6O1VDgk7QeCNK1Xy3OmnK_6rA0DN3mynl6mHobIxpUhdAw5MZXFkNAUmsTa24LCoABXY0QDnEmAm4_rr-9wnT4CDwsdIpTxXpQoMfcJzdlERuuKrXuRJhLvzda4rCI38NFs6P2w6bCEcrXpAv64G2FB_SSxC9VSTlrCEPpyNVkKUrj_-5Wcpr8z-2-qgIcLjeofNpNFSz8QpYXliuXW2H2tjlzha-JJ4wVSXlKERztkVAq5RYtpW2lrR9Yy3fgdTiaPL2d4BNtN8GyGKg_3dub99dAf39PsCkZTpTwCT5QFGSSwp278ihyxD3PysOWnUumLCspF4cZ2dF416kmR38e58wqBfXpIFPXlr_o4zrVQ11RvqNp76kpIScjNatwQRcdwAV9micqE6rAl2TGS_vELhHqeFjprGaxqifpnOFG01IrLmWVC5X6Reg1JXXPfWqb9sklcoVFUBYtM0NX0dkYoSRJdtcyDc2jvtMDrRKVtZR09k0pMmPlglliGOyOAqGOz94AZZoBTA==) |
| Model download | [model-download-flow.puml](docs/puml/model-download-flow.puml) | [SVG](https://kroki.io/plantuml/svg/eNp9VE1v2kAQve-vGHECqcR3FFCiUBeq0FYBcuplscf2qsuutbuESlH-e2c_TAwh5WQ8b97Me2_gzjpu3GEvYa9LlONSH5XUvBxXUh-ZE04irHwF5qkCua8wXjhtYGvRsJYYRCFarhwMAniNEgsntFoXBlENgFvYLq8AO9IVV7xGE4Dp-Rz9hHvtMJLrgykwQONbVnLHd9wiDBaHuhaqznkCLPJe8VEXXMKa9ib-UE7PjE2n8NBg8QcqL6qlHrQwnTKvD8YzWn4CG97C4ANqwLZLj0hbT6DwiFybVB-OWCp5VFx4AhW6ormXsifLI6XWLVAvIC8a6KllQJ8I9jSLfALfvm4g463IQnA2eyUr0bxlrwZb_ZY5Mj7bc6FC6yKHcW_89_XPH1AJylYK6xiqknXkfSkr3t72lvgCj4S-jcicumezvrZT24PeU3bEHfz2YywchWvAxLhOPcnY6BR0p7d2wf1hfH3_woXkO4nZtt3oOb0ZhbhO5xiv8yKreIDxpmMiO_33MqluYGAYBuxZWN7l05ibnVBBC1PeJyPqxtEcspnvLTid1Np4Ub9VENwaXRu0Fg7xGD5In_c133Tf6IKHXeuIpezO7JXY93HWHTItxF8wyQ7L_n9gR1UGS5NpVw31x9_5hWWckPz8ZbBCg6pA-yzwGNonYNG9F4Y2UGO0elmOLkzUhjjJX4qXh28xYvS7Xd9nq0Kqn-30nnHg-Czhk21p1FXLnrCiIJre74Xd0S-G_jPZPw_qxi8=) |
| Download states | [download-states.puml](docs/puml/download-states.puml) | [SVG](https://kroki.io/plantuml/svg/eNqFVE2P0zAQvftXzHEXqaty9QEt2qViJbpIfJwQBxNPUmsTu7InrQDx3xk7cWzSSvTk8Ty_-XgvvQ-kPI1DD9qdbe-U3vANYRBkqEfYO409PM45-BxzsFfNwVgU4tur77DZvIEn3XOUHqYz_BbAv3iUM4V1tJRAXaWd5xyohoyzYCwcves8hiD-ZMaHAzYvxnYzaw4lPLjhqHzMnA0dwOPgCFeYHVJstoPWcF8DktKKVCH_euQY356U6dWPpfPVrYRPiXsi0aZt0YfrwNa7AXrXqB5ODOKh6lpf3GM85CJTKOFDwg-KW8VQz1Egz8uOLGLc4MKaxSkbqm7qBcTO3o9dx8FONXgJzavnR3xHcLO9225e321vKy145T0S6ixGjmUxSWusCYdZ5Aqwi8sL6sSPyUEg51WHhfqd9-yFiTadJcsVAmMk-y7KXKeekc7Ov0T7PH2Elvc_-kSW_BdNudhGQhOPO-cntcLNbUHV25OLQ5NnGSYWkohde0VCML9wb0JSbo2dtZ5Bl4hpXsl6pklSKMS6xv97vPYijcdQbs3wpySWbqokmQHdSELU9JflsilgTHUu0MURPOnYNPHTXWPyqFkmUR5VDXEVJDFhrwxREv826JH8TyHu0Wr-HxN_ATb2mUM=) |
| Model classes | [model-classes.puml](docs/puml/model-classes.puml) | [SVG](https://kroki.io/plantuml/svg/eNqdVktz2jAQvvMrPDlBG1_SG8OQ8DDEUxtSHsn0xAh7ATWy5UqiaZLJf68eEMvYHphygt1P3z74dqU7LhAT-4Q4CY2BuBFBnANvCCwIOKGyOSFK0RaYM1A-Z4jRlqGk0chQ9CztzpU-mRjQlfPeaDjyo4mcpx3mGTDD864d6vMVx21nLhhOt7ktllCCXicogQon8IjhTGCalp0Epdu9jM7bToC56Bh_Nwdw_Ab9V6EBtEBLX1JCUbxkpEy7wQSqs_m9R6nAb8hO51a7P0z1dP0LIuH0_iBM0JqAbgC3O7DwJz9X4TJY-IE_GS97QbvQrBOgN6lz93tz7yKeedgLgouQoTf0l-FF0KA3G3urx5sz_m_n_KvFctaf1qHuvf7Me1r5jzN_sfpx8_0iXG1Q181P6JZMh14wP0jHPmEJaAui_-rHTRy3iqy3BcwQNmhPRLNVl-GGsgSJuZRjc6302Cpo6yAeSPeJGb053bMILNk83fvzB2-2Gjw8fNpMueNxGJyYer5sQpXNdLuyIQwyOn1JgZVFr1zV47BmKI12ZTvKcOVgHaeujzicAj7s9TGDhAoYyTm0R6d-LjMkKtL4v-nnuvdt-48oJzg8MEB8eZK16UhlRDuIdcBalR1AeWfaVpeqdpDxVsvp6wYkm1lOI0YTA2ia0luKmUs5d_Rk5FG63ROCHiFWFK7kH6KsY8W8rqWyuXB6qCtURTaPLbx21DfeKldadWxETc-a-lo6czYFiPkyi5GAJqERImo0rx2msfJQn1ICKLXbitZcMCQbW1TAXEiOQ2s_LIX4sdQF_BWQxidgCzTYQfQsRXIWaFL9vFYuwC_oUCV2Dni01yZhqyZjdMuASwGPpF-Uah7QJCMgID4b1mOMsvMBExlM3u_1i0L_48fjx_dKTuBGNBUySlumpr_kHq0SPsRy4Slt5I7YTkaWGu6Farn-Kct-6UiJd60IqDiWx7eIpfXKhXsMY2lW6m6ZYmE_flQ3iwhbmeZJxQtNgLgeSmRixc2lZ1bnW7Rb0xkpjcrhMhLUB4pZyvtvyrIdSi-mNHWVDn3SHv7i01fUF9ctX6_WVeG63dLWPlnUClKiqMDkrBpRKTOFK-u7Flrax5cxm6QasiV3clbkg73xD-FQi0Y=) |
| Preferences flow | [preferences-flow.puml](docs/puml/preferences-flow.puml) | [SVG](https://kroki.io/plantuml/svg/eNp1U8GK2zAQvesrBp-ysDaF0kuoly2kC6GkXWqcXgJFsSeJqC2p0jhpKPvvHcnZrtxubmbemzfz3sj3nqSjoe_AOtyhQ92gz3edOQlS1CE8vpRhIUnCQ8CE5S7VKCs1QVYvoWocos5AeqiXUzRRWCs8rUyLXSSuV1NikK_IOIzoohItF7bSI2TJcoXdRvxBdShEWcJXlO1kzbIUvFF-xwPm0Jiuw4Y--Iok4exGrFcBWlRzCPJFLy38Ts3Dk1hUgRIGzEd16wyZ7bAToQb5pT-Z-RgIse8yNqT0PiHcxbkM1stJY63iXtHIN6coDXxqxCO9QLMfeL6Fo-wGTB0Nlj1hPNPoyQdkamic8rqjamh4J58aqaNkO5498fAZT9d8xAtDhSF3ZTR8_CV720U72vBsc0QXNWrPHz7yPPSh66rfjYYLE9uov2xvQy3rpNtjfnyb0-C2Jv_57vubbKOvhqKoaIw9v6YH5Rgodz_9e0rAXvGOmk1HTpoEr-ywMb01nl_PSdFhowPxWf7iLCSTvtJPePZ_I3FqfyAwu_Dq_1uM70JO6T0jsy904MyktemTLYriRqBuIWgJcc-f_EeLP-8YQ1I=) |

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
