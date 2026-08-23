# Fabric Focus

Offline-first Android study companion for Microsoft DP-600 practice material.

## Stack
- Kotlin + Jetpack Compose + Material 3
- Bundled SQLite question bank
- Local linked question and explanation images

## Open in Android Studio
1. Open the `FabricFocus` folder.
2. Set the Gradle JDK to JDK 17 or newer.
3. Allow Android Studio to sync the project and install the requested Android SDK platform (API 35).
4. Run on an Android 8.0+ device or emulator.

The app imports `app/src/main/assets/dp600.sqlite` into its private storage on first launch. Image relations are preserved through the database's `question_images` table and image assets under `app/src/main/assets/images/`.

## Current scope
- Dashboard with topic coverage
- Offline question practice
- Answer review and explanations
- Diagram support for questions and explanations
- Dark Material 3 visual design

## Planned next milestones
- Persistent attempts, bookmarks, and review queue
- Topic/difficulty filters and timed mock exams
- Progress analytics and streaks
- Database update/import workflow
