# Piano Master — Technical Specification

## Product goal
A single-user Android piano academy for a physical 37-key keyboard. The app teaches note names, technique, theory and songs; tracks practice; and provides feedback from microphone audio now, with MIDI input supported as a future hardware path.

## Critical product constraint
The current keyboard connection is audio output only. Audio is not MIDI: the app cannot know the exact key switch or velocity. Microphone analysis estimates pitch from sound and is vulnerable to room noise, harmonics, sustain, reverb and overlapping notes. MVP feedback must be labelled as estimated and monophonic. Exact polyphonic note and velocity feedback requires MIDI input.

## Architecture
- Presentation: Jetpack Compose screens, navigation, ViewModels, UI state.
- Domain: note model, keyboard range, lesson engine, song event model, scoring rules and use cases.
- Data: Room database, DataStore settings, bundled JSON lessons/songs, repositories.
- Audio: AudioRecord PCM capture, pitch estimator, confidence/stability filter, note matcher.
- MIDI: Android MidiManager discovery/open/receive, optional USB/Bluetooth MIDI.
- AI: Tutor repository calling a server-side API proxy; no provider secret in APK. Offline fallback uses curated lesson explanations.

## Screens
1. Onboarding: goals, experience, handedness, daily target.
2. Keyboard setup: 37 keys, lowest/highest MIDI note, tuning reference, audio input test, connection mode.
3. Home: next lesson, practice streak, daily goal, recent songs, skill gaps.
4. Learn: course map, lesson steps, note diagrams, finger guidance, checks and completion.
5. Piano: 37-key map, octave labels, note/frequency display, virtual key taps.
6. Audio practice: mic permission, input meter, detected pitch, confidence, target-vs-detected feedback.
7. MIDI devices: discover, connect, disconnect, input status; hidden/optional until hardware available.
8. Songs: categories, difficulty, range compatibility, left/right hand, tempo and section loop.
9. Song player: score/keyboard lane, count-in, tempo control, step/loop, note feedback.
10. Chords & scales: fingering, formula, inversions, practice mode.
11. Metronome: BPM, beat grouping, subdivision and count-in.
12. Progress: practice duration, lesson completion, accuracy trend, weak notes and streak.
13. AI tutor: ask questions, explain errors, create practice plan, recommend next activity.
14. Settings: keyboard range, audio calibration, theme, privacy, export/backup.

## Database (Room)
- PracticeSession(id, startedAt, endedAt, durationSeconds, lessonId, source, notesAttempted, notesCorrect, estimatedAccuracy)
- NoteAttempt(id, sessionId, timestamp, expectedMidi, detectedMidi, confidence, timingErrorMs, result, inputSource)
- LessonProgress(lessonId, status, currentStep, completedAt, bestScore, lastPracticedAt)
- SongProgress(songId, status, bestTempo, bestAccuracy, lastPosition, completedAt)
- UserGoal(id, dailyMinutes, preferredHand, skillLevel)
- Song metadata and lesson content should initially be bundled JSON; later may be cached in Room.
- DataStore: keyboardLowestMidi, keyboardHighestMidi, preferredHand, dailyGoalMinutes, audioSensitivity, theme, onboardingComplete.

## Lesson engine
- Lessons are versioned JSON sequences of instruction, target note/chord, suggested fingers, tempo, repetitions, success rule and hint.
- State machine: NOT_STARTED → IN_PROGRESS → STEP_PASSED → COMPLETED.
- A step passes by explicit self-check for theory tasks or by configured evidence for practice tasks.
- Audio mode: accept a detected pitch only after confidence threshold and stable consecutive frames; compare pitch class and octave separately. Use tolerances and explain uncertain readings.
- MIDI mode: compare exact note-on/off events and timing; velocity is available when supported.
- Avoid blocking progression solely on noisy microphone recognition; allow manual confirmation.

## Audio/MIDI integration
- AudioRecord mono PCM16 capture at a supported sample rate; request RECORD_AUDIO at runtime; start/stop with lifecycle.
- Pitch estimation: MVP monophonic autocorrelation; production recommendation YIN/MPM plus confidence, harmonic rejection and temporal smoothing.
- Audio limitations: no guaranteed polyphonic transcription, no exact velocity, no direct key event.
- MIDI: Android MidiManager device enumeration and MidiDevice open; parse note-on/note-off, channel, velocity and timestamps; support reconnect and device removal.
- Input abstraction: PianoInputSource interface with MicrophonePitchSource and MidiInputSource implementations.

## AI tutor
- Context includes active lesson, keyboard range, recent error categories and skill level; avoid sending raw microphone audio by default.
- API calls go through a backend proxy with auth, rate limits and secrets stored server-side.
- Offline fallback provides deterministic hints and lesson explanations.
- AI can explain concepts and propose practice plans, but should not claim audio-derived exact note certainty when confidence is low.

## Privacy and security
- Microphone permission is requested only when the user starts audio practice.
- Process audio locally; do not persist or upload raw audio by default.
- Provide clear mic-active state and immediate stop control.
- No API keys or private credentials embedded in the APK.

## Phased implementation
- P0 foundation: Compose shell, navigation, keyboard range setup, note model, lesson catalog, Room/DataStore.
- P1 learning MVP: note map, guided lessons, practice timer, metronome, bundled starter songs, progress tracking.
- P2 audio feedback: mic permission, calibration, monophonic pitch estimator, confidence/stability, target note feedback.
- P3 MIDI support: device discovery, event parsing, note-on/off scoring, reconnect handling and hardware tests.
- P4 AI tutor: secure backend, contextual Q&A, personalized plan, offline fallback.
- P5 polish: tests, accessibility, localization, backup/export, crash reporting, signed release APK.

## Acceptance criteria for MVP
- App launches and navigates without network.
- User can set actual lowest/highest note of their 37-key keyboard.
- Note names and octave labels are correct for configured range.
- Lesson progress survives app restart.
- Practice duration and attempts are persisted.
- Microphone is not accessed before explicit permission and user action.
- Audio feedback is labelled estimated/monophonic.
- AI secrets are not present in APK.

## Release 1 implementation status (2026-11-08)

Implemented in source: Compose navigation, home dashboard, six beginner lesson modules, configurable 37-note visual map, microphone-based monophonic pitch estimator and practice UI, manual attempt scoring, metronome, three starter melodies, offline FAQ tutor, and local SharedPreferences progress counters.

Not implemented in this release: full piano sound synthesis, polyphonic audio transcription, true generative AI tutor, USB/Bluetooth MIDI event handling, cloud sync, and full Room-backed practice history. These remain planned items and must not be represented as available features.

Audio-output-only keyboards do not expose key events to Android. Microphone pitch estimation is an approximation and cannot guarantee which physical key was pressed, especially for chords or noisy environments. Exact feedback requires a compatible MIDI connection.
