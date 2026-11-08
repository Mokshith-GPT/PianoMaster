# Piano Master (Android)

A personal piano-learning app for a 37-key keyboard, built with Kotlin and Jetpack Compose.

## Included in Release 1 source

- Home dashboard with locally stored lesson/practice counters
- Guided beginner lesson path (notes, five-finger position, note patterns, C major scale, triads, first melody)
- Adjustable 37-note keyboard map (default C3–C6; change the lowest MIDI note to match the instrument)
- Microphone-based monophonic pitch estimation for one note at a time
- Target-note practice with manual attempt recording
- BPM metronome
- Starter note-name song arrangements
- Offline piano help for common beginner questions
- Local progress metrics
- GitHub Actions workflow to compile a debug APK in the cloud without Android Studio

## Important input limitation

The current instrument connection is audio output only. Android cannot infer the exact key switch that was pressed from that connection. The app listens with the phone microphone and estimates a dominant pitch. This is best-effort, monophonic feedback; it is not polyphonic transcription or exact MIDI event detection. MIDI support is a future hardware integration and will require a compatible USB/Bluetooth MIDI connection.

## Build without Android Studio

See [BUILD_WITHOUT_ANDROID_STUDIO.md](BUILD_WITHOUT_ANDROID_STUDIO.md). The repository includes a GitHub Actions workflow that builds `app-debug.apk` and publishes it as a downloadable workflow artifact.

## Build locally (optional)

Requires JDK 17, Gradle 8.9, and Android SDK Platform 35:

```bash
gradle --no-daemon assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## AI tutor status

The bundled tutor is a local rule-based helper that works offline. A true generative AI tutor must call a secure backend. Never ship an OpenAI or other provider secret in an Android APK.

## Release status

This is a Release 1 source build, not a verified binary. The source archive has been checked for ZIP integrity, but it has not been compiled or device-tested in this environment because the Android SDK and Gradle are unavailable here.
