# EcoRoute Madrid 🌍♻️

##Repositories:
-GitHub repository : https://github.com/GindCake/MAD_Project

EcoRoute Madrid is a modern Android application designed to promote sustainability and reward eco-friendly habits within the university campus. The app encourages users to recycle by gamifying the experience through points, levels, and a global ranking system.

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

## 🔧 Setup
1. Clone the repository.
2. Add your `google-services.json` to the `app/` directory.
3. Add your `MAPS_API_KEY` to `local.properties`.
4. Build and run on an Android device with Google Play Services.

## PARTICIPANTS
-iliyan.dimitrov@alumnos.upm.es
-a.astoyanov@alumnos.upm.es

---
*Developed for a greener future at Madrid's University Campus.*
