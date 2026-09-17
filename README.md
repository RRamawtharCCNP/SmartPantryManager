# Smart Pantry Manager

Smart Pantry Manager is an Android application built in Java that tracks leftover pantry ingredients and dynamically suggests recipes for which **every** required ingredient and quantity is currently available in the user's pantry.

## Features

- **Pantry Tracking**: Add, edit, and delete pantry items with exact quantities, measurement units, and expiration dates.
- **Strict Recipe Matching**: Evaluates pantry items against pre-seeded recipes using exact unit normalization and quantity requirements.
- **Recipe Details**: Displays complete ingredient checklists and step-by-step cooking instructions.
- **Dark Mode Support**: Styled using modern Material Design card layouts with support for night themes.
- **Offline Storage**: Powered by local SQLite database storage for zero cloud latency and full privacy.

## Architecture & Database Design

The application utilizes an offline-first architecture using local SQLite via `SQLiteOpenHelper`.

- **Pantry Table (`pantry`)**: Stores active ingredient records with name, quantity, unit, and expiration date.
- **Recipes Table (`recipes`)**: Stores dish names and detailed preparation methods (18 default recipes seeded on first run).
- **Requirements Table (`requirements`)**: Foreign-key linked table detailing required ingredients, exact amounts, and measurement units for each recipe.

### Strict Matching Mechanics

1. **Name Normalization**: Ingredient names are normalized to ignore case, whitespace, punctuation, and common singular/plural variations.
2. **Unit Conversion**: Measurement units are converted within compatible families (e.g., `kg` to `g`, `l` to `ml`). Incompatible unit families are safely ignored.
3. **Quantity Threshold**: Recipes only appear in "Suggested Recipes" when the total available quantity in the pantry meets or exceeds the recipe requirement.

## Requirements & Building

- **IDE**: Android Studio Ladybug (or newer) with JDK 17
- **Target SDK**: Android SDK 35
- **Minimum Supported OS**: Android 7.0 (API Level 24)

### Quick Run Guide

1. Open `SmartPantryManager` in Android Studio.
2. Let Gradle sync dependencies and build configuration.
3. Launch an emulator or connect a physical Android device running API 24+.
4. Execute the `app` run configuration.

## Verification & Academic Submission

- **GitHub Repository**: Linked to official incremental commit history.
- **Video Walkthrough**: Includes a 1-minute GitHub commit walkthrough demonstrating project evolution.

