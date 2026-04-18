# EcoRoute Madrid 🌍♻️

## Repositories:
-GitHub repository : https://github.com/GindCake/MAD_Project
## WorkSpace:
https://upm365.sharepoint.com/sites/KotlinProjectiliyan.alex/SitePages
## Description:
EcoRoute Madrid is a modern Android application designed to promote sustainability and reward eco-friendly habits within the university campus. The app encourages users to recycle by gamifying the experience through points, levels, and a global ranking system giving them a discount QR codes for the UPM Food Places.

## Screenshots & Navigation:
<table style="width: 100%; border-collapse: collapse;">
  <tr>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/446c604a-71da-46b0-b7ad-3acd068cdb7d" width="100%" />
      <p><i>Home page not signed in</i></p>
    </td>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/8eab5249-33b6-45c8-a4e0-da73a6532a6a" width="100%" />
      <p><i>Google Authentication Window</i></p>
    </td>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/a1093025-848c-44ae-bb3c-c3c5ce2dbf80" width="100%" />
      <p><i>Home page with an account signed in</i></p>
    </td>
  </tr>
  <tr>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/ef2d33c2-c162-484f-82a0-ed23f15cc58b" width="100%" />
      <p><i>Ranking Page by the recycled points</i></p>
    </td>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/651ba6e9-a994-4c30-9267-e6a735c4adbd" width="100%" />
      <p><i>Recycling Map with all the locations of the trashbins</i></p>
    </td>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/5beb79ed-d7d4-4ec1-acf8-d53f810d9dc4" width="100%" />
      <p><i>Recycle Bin Overview</i></p>
    </td>
  </tr>
  <tr>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/acc759cf-826b-4fdf-9ae8-027e14459ee7" width="100%" />
      <p><i>Pop notiffication for the earned points</i></p>
    </td>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/10052116-8640-43bb-bb7f-4fbffeefaf10" width="100%" />
      <p><i>Profile Page</i></p>
    </td>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/05f225f9-595c-49a1-ada9-da8e99d86091" width="100%" />
      <p><i>Reward System Overview</i></p>
    </td>
  </tr>
  <tr>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/e9ccba4f-9404-4875-8edc-1bee6e3721eb" width="100%" />
      <p><i>Weather Forecast</i></p>
    </td>
    <td align="center" style="width: 33%; vertical-align: top;">
      <img src="https://github.com/user-attachments/assets/978cc736-a534-4ccd-93d6-715f5c23af43" width="100%" />
      <p><i>Log in info list</i></p>
    </td>
    <td style="width: 33%;"></td> </tr>
</table>

## Demo Video:
https://upm365-my.sharepoint.com/personal/iliyan_dimitrov_alumnos_upm_es/_layouts/15/stream.aspx?id=%2Fpersonal%2Filiyan%5Fdimitrov%5Falumnos%5Fupm%5Fes%2FDocuments%2FScreen%5Frecording%5F20260419%5F011546%2Ewebm&referrer=StreamWebApp%2EWeb&referrerScenario=AddressBarCopied%2Eview%2Eb37700d2%2D760e%2D47b4%2Db509%2D304e8323972c&isDarkMode=false


## 🚀 Key Features

### 1. User Authentication & Profile
*   **Google Sign-In Integration:** Secure and easy login using Google accounts.
*   **Real-time Profile Sync:** User data, points, and progress are synced instantly with Firebase Firestore.
*   **Persistent Progress:** Track your total points, current level, and daily streaks across devices.

### 2. Interactive Recycling Map
*   **Google Maps Integration:** View all recycling bins across the campus in real-time.
*   **Smart Proximity Detection:** The app uses high-accuracy GPS to detect when you are within 10 meters of a recycling bin.
*   **Contextual Recycling:** When near a bin, a dialog appears allowing you to "Recycle" and earn points based on the bin type (Paper, Glass, Plastic, Organic, Battery, E-Waste).

### 3. Advanced Reward System (`RewardManager`)
*   **Point Scaling:** Different bin types yield different points (e.g., Electronic Waste offers higher rewards than Organic).
*   **User Tiers:** Progress through three distinct levels:
    *   **Eco-Conscious:** entry level, 5% campus discount.
    *   **Eco-Warrior:** 151+ points, 10% campus discount.
    *   **Planet Guardian:** 501+ points, 15% campus discount.
*   **Daily Streaks:** Maintain a recycling habit! Reaching a 5-day streak triggers a **Points Double** bonus for every recycling action.
*   **Reward QR Codes:** Generate a dynamic QR code based on your current tier to redeem discounts at participating campus locations.

### 4. Global Ranking & Competition
*   **University Leaderboard:** Compete with other students and staff. The ranking system calculates your position in real-time based on total points accumulated.
*   **Progress Tracking:** Visual progress bars show exactly how many points are needed to reach the next tier.

### 5. Campus Insights
*   **Weather Integration:** Check current weather conditions directly in the app to plan your eco-friendly route.
*   **Detailed Locations:** View a comprehensive list of all recycling points with addresses and bin types.

## 🛠 Technical Stack
*   **Language:** Kotlin
*   **Backend:** Firebase (Authentication & Cloud Firestore)
*   **Maps:** Google Maps SDK & Google Play Services Location
*   **Architecture:** MVVM (Model-View-ViewModel) pattern with Repositories
*   **Local Data:** Room Database for offline caching
*   **UI Components:** Material Design 3, Bottom Navigation, Navigation Drawer, and Expandable Bottom Sheets.

## 📱 App Structure
*   **MainActivity:** Dashboard with quick actions, streak count, and current ranking status.
*   **MapFragment:** The core interactive experience for finding and using bins.
*   **SettingsActivity:** Comprehensive profile view with tier progress, point totals, and QR reward generation.
*   **RankingActivity:** Real-time global leaderboard.
*   **WeatherActivity:** Environmental data for the Madrid area.

## Firebase Setup:
1. Create a Firebase project at firebase.google.com.
2. Register an Android app with package name com.example.myapplication.
3. Download and place google-services.json in app/google-services.json.
4. Enable Authentication → Sign-in method → Email/Password.
5. Create a Cloud Firestore database (Standard edition, locked mode recommended).
6. Publish the following Firestore security rules:
```
 service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to read and write only their own document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read the entire users collection for the ranking
    match /users/{document=**} {
      allow read: if request.auth != null;
    }
  }
}
```
## Run Locally

1. Open the project in Android Studio (latest stable recommended).
2. Sync Gradle.
3. Complete Firebase Setup (see above).
4. Run on emulator or physical device:
    Via terminal: ./gradlew installDebug
    Or press Run in Android Studio.

## PARTICIPANTS
1. Iliyan Nikolov (iliyan.dimitrov@alumnos.upm.es)
2. Aleksandar Stoyanov (a.astoyanov@alumnos.upm.es)

---
*Developed for a greener future at Madrid's University Campus.*
