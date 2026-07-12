# Omnitrix Watch App

A fan-made Omnitrix-style launcher app for the **Modio 4** (Android 9, square screen).
100% offline — no INTERNET permission is even requested, so there's nothing for it to
call home to.

## What it does

- **Home screen** — a glowing hourglass ("Galvin") symbol, pulsing continuously.
- **Tap the symbol** — the dial expands and reveals a ring of 10 alien slots around it.
- **Drag / swipe** anywhere while the ring is open — rotates the wheel by touch, exactly
  like spinning the real dial. It snaps to the nearest alien and gives a soft click.
- **Tap the ring** — selects whichever alien is currently under the top marker, plays a
  synthesized "transform" power-up sound, and opens a full-screen glowing view of that
  alien.
- **Tap the center again** while the ring is open — collapses it back to the home symbol.
- **Long-press anywhere** on the home screen — cycles through three modes, each with its
  own tint and pulse speed:
  - **READY** — green, normal idle glow
  - **NEW DNA SCAN** — yellow, fast pulse
  - **LOW POWER** — red, slow dim pulse

## Why the sound isn't from the show

I can't bundle or reproduce actual audio from the original series — that's copyrighted.
Instead, `SoundGenerator.kt` synthesizes short sci-fi tones (a rising sweep for
transformation, a short click for UI feedback) directly in code with `AudioTrack`. No
audio files, nothing downloaded, works fully offline.

## Artwork

- `alien_01.png` … `alien_07.png` and `omnitrix_symbol.png` are your uploaded images,
  processed so the black background is transparent (so they can be tinted per-mode and
  glow properly).
- `alien_08.png` … `alien_10.png` are simple placeholder silhouettes I generated so all
  10 dial slots are functional out of the box. Swap them for your own transparent PNGs
  (same file names, in `app/src/main/res/drawable-nodpi/`) any time — no code changes
  needed.
- Rename the on-screen labels in `Alien.kt` (`AlienRoster.ALL`) if you want custom names
  under each glow screen.

## Building locally

1. Open the `OmnitrixWatch/` folder in Android Studio (Giraffe or newer). It will offer
   to generate the Gradle wrapper automatically the first time you open it — accept that.
2. Build → Build Bundle(s) / APK(s) → Build APK(s).
3. The APK lands in `app/build/outputs/apk/debug/`.

If you'd rather use the command line and already have Gradle installed:
```
gradle wrapper --gradle-version 8.4
./gradlew assembleDebug
```

## Building automatically with GitHub Actions

This repo includes `.github/workflows/build.yml`. It runs automatically on every push
to `main` (and can also be triggered manually from the **Actions** tab → *Build Omnitrix
APK* → *Run workflow*). It:

1. Sets up JDK 17 and the Android SDK.
2. Generates the Gradle wrapper if it's missing.
3. Runs `./gradlew assembleDebug`.
4. Uploads the resulting APK as a workflow artifact named **omnitrix-debug-apk** — download
   it from the finished run's **Artifacts** section.

To use it:
1. Push this project to a new GitHub repository.
2. Go to the **Actions** tab — the workflow runs automatically.
3. Open the latest run, download the `omnitrix-debug-apk` artifact, unzip it to get the
   `.apk`.

## Installing on the Modio 4

1. Copy the `.apk` to the watch (via USB file transfer, or a file manager app that can
   read a microSD/USB drive plugged into it — no internet needed either way).
2. On the watch, enable **Settings → Security → Unknown sources** (or "Install unknown
   apps" for the file manager you're using) since this isn't from the Play Store.
3. Open the APK from a file manager on the watch and install it.
4. Set it as your default launcher/home app if you want it to appear on boot
   (Settings → Apps → Default apps → Home app), or just launch it manually.

## Notes

- `minSdk`/`targetSdk` are pinned to API 28 (Android 9) to match the watch exactly.
- The UI is drawn entirely with Canvas at runtime and scales to any square resolution,
  so it should look right regardless of your Modio 4's exact panel size.
- No permissions beyond what's built into the OS are requested — check
  `AndroidManifest.xml` any time to confirm there's still no `INTERNET` permission.
