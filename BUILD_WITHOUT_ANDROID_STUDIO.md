# Build Piano Master APK without Android Studio

This project includes `.github/workflows/build-apk.yml`. GitHub Actions can compile a debug APK in the cloud.

1. Create a new GitHub repository in your account.
2. Upload the contents of this project ZIP to the repository root (do not upload the ZIP as a single file).
3. Open the repository's **Actions** tab and enable Actions if GitHub asks.
4. Select **Build Piano Master APK** and choose **Run workflow**. A push to `main` or `master` also starts a build.
5. When the run finishes successfully, open the run and download the `Piano-Master-debug-apk` artifact.
6. Extract the artifact ZIP on your phone and install `app-debug.apk`. Android may ask you to allow installation from that browser/file manager.

The resulting debug APK is signed with Android's standard debug key. It is suitable for personal testing, not Play Store distribution. A release APK should use a private signing key that you control.

## Important limitations in this first release

- Audio note detection is monophonic / best-effort and depends on microphone placement and room noise.
- The 37-key map defaults to C3–C6, but the lowest note is adjustable because 37-key keyboards do not all share the same pitch range.
- The tutor is an offline rule-based helper, not a connected generative AI service. A real AI tutor needs a secure backend and your own API credentials; do not embed secrets in the APK.
- MIDI input is not active for an audio-output-only keyboard. The app architecture retains a future integration point for USB/Bluetooth MIDI hardware.
- The note map is a visual learning aid; this version does not synthesize a full piano sound.
