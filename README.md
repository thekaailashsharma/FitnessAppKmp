# Fitness App KMP

A multiplatform fitness application built with Compose Multiplatform.

## Avatar Implementation

The Fitness Buddy feature currently uses emoji-based avatars. To implement custom avatar images:

### Option 1: Add Resource Files
1. Create a directory: `composeApp/src/commonMain/resources`
2. Add the following files to this directory:
   - Create a `drawable.xml` file with your avatar resources
   - Follow the Compose Multiplatform resource naming conventions

### Option 2: Use Platform-Specific Resources
For platform-specific implementations:
1. Android: Add images to `composeApp/src/androidMain/res/drawable/`
2. iOS: Add images to `composeApp/src/iosMain/resources/`
3. Desktop: Add images to `composeApp/src/desktopMain/resources/`

### Option 3: Use Network Images
If you prefer to use remote images:
1. Implement a network image loading library like Coil or Kamel
2. Update the AvatarImage component to load from URLs

## Features

- Workout tracking and planning
- Calorie and nutrition monitoring
- Community sharing and social features
- Motivational Fitness Buddy with chat interface
- Progress tracking and statistics

## Getting Started

1. Clone the repository
2. Open in Android Studio or IntelliJ IDEA
3. Run on your preferred platform (Android, iOS, Desktop)

## License

[License details here]