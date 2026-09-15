# Smart Pantry Manager

Smart Pantry Manager is a Java Android application that tracks leftover ingredients and suggests only recipes for which every required ingredient and quantity is already available.

## Database choice

The app uses local SQLite through `SQLiteOpenHelper`. SQLite suits a private pantry app because it works offline, adds no account or hosting dependency, persists between launches, and supports transactional CRUD. Three related tables store pantry items, recipes, and recipe requirements. Eighteen recipes are seeded on first run.

## Requirements

- Android Studio with JDK 17
- Android SDK 35 (minimum supported device: Android 7.0 / API 24)

## Run

1. Open the `SmartPantryManager` folder in Android Studio.
2. Allow Gradle sync to complete.
3. Select an emulator or Android device running API 24 or later.
4. Run the `app` configuration.

## Demonstrating strict matching

1. Add 2 eggs and 30 ml milk. “Scrambled Eggs” appears.
2. Edit milk to 20 ml. The recipe disappears because quantity is insufficient.
3. Restore milk to 30 ml. The recipe returns.
4. Delete the eggs. It disappears again.

Name matching normalises case, whitespace, punctuation, and common singular/plural forms. Quantities normalise kg to g and l to ml, while incompatible measurement families never match.

## Academic submission checklist

- Replace identity placeholders in the report.
- Add a public GitHub repository URL and push genuine incremental commits.
- Capture real screenshots from the running app and place them in the report.
- Record the required narrated 5–7 minute demonstration.
- Sign the declaration yourself and create the final ZIP below 50 MB.
