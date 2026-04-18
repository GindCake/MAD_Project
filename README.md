# Eco-Conscious Recycling & Weather Tracker

An Android application designed to promote environmental awareness through recycling rewards and local weather tracking. The app helps users locate recycling bins, earn points for recycling, and stay updated with weather conditions.

## Features

### Functional
* **Interactive Map**: View recycling bins on an interactive map with proximity-based alerts.
* **Recycling Rewards**: Earn points by recycling at designated bins (Paper, Glass, Plastic, Organic, Battery, E-Waste).
* **Weather Tracking**: Real-time weather updates to help plan outdoor recycling activities.
* **User Levels**: Level up your "Eco-Conscious" status as you accumulate recycling points.
* **Reward QR Codes**: Generate QR codes based on your current eco-level for potential reward redemption.
* **Google Authentication**: Easy sign-in and account management using Firebase and Google Auth.
* **Location Services**: Real-time proximity detection for nearby recycling facilities.

### Technical
* **Firebase Authentication**: Secure Google Sign-In integration.
* **Cloud Firestore**: Real-time database management for user profiles, points, and recycling bin data.
* **Google Maps SDK**: Interactive mapping and marker management.
* **osmdroid**: OpenStreetMap integration for alternative map views.
* **Retrofit + Gson**: Asynchronous networking for weather data retrieval.
* **Room Database**: Local caching of location records and recycling history.
* **Material Design 3**: Modern, clean UI following Material 3 guidelines.
* **Architecture**: MVVM pattern with ViewModels, Repositories, and clean separation of concerns.

## How to Use
1. **Sign In**: Launch the app and sign in using your Google account to track your recycling progress.
2. **Enable Location**: Grant location permissions to see nearby recycling bins and receive proximity alerts.
3. **Explore the Map**: Use the interactive map to find the nearest recycling stations.
4. **Recycle & Earn**: When you are within 10 meters of a bin, a dialog will appear allowing you to "Recycle" and earn points.
5. **Check Weather**: Use the Weather action to see current conditions before heading out.
6. **Track Progress**: View your current eco-level and total points on the home screen.

## Firebase Setup
1. Create a Firebase project at [firebase.google.com](https://firebase.google.com).
2. Register an Android app with package name `com.example.kotlin_project_1`.
3. Download and place `google-services.json` in `app/google-services.json`.
4. Enable **Authentication** -> **Sign-in method** -> **Google**.
5. Create a **Cloud Firestore** database.
6. Publish the following Firestore security rules:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Run Locally
1. Open the project in **Android Studio** (Koala or newer recommended).
2. Sync Gradle files.
3. Add your `MAPS_API_KEY` to `local.properties`:
   `MAPS_API_KEY=your_google_maps_key_here`
4. Complete the Firebase Setup (see above).
5. Run on an emulator or physical device via Android Studio or terminal:
   `./gradlew installDebug`

## Requirements
* **JDK**: 11
* **minSdk**: 24
* **targetSdk**: 36
