package com.example.kotlin_project_1

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Calendar

class RewardManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun calculateUserTier(points: Int): UserTier {
        return when {
            points <= 150 -> UserTier("Eco-Conscious", "5%", 0, 150, 1)
            points <= 500 -> UserTier("Eco-Warrior", "10%", 151, 500, 2)
            else -> UserTier("Planet Guardian", "15%", 501, 1000, 3)
        }
    }

    fun getPointsForBin(type: BinType): Int {
        return when (type) {
            BinType.BATTERY, BinType.E_WASTE -> 15
            BinType.PLASTIC -> 10
            BinType.GLASS -> 7
            BinType.PAPER -> 5
            BinType.ORGANIC -> 3
        }
    }

    fun generateQRCodePlaceholder(tierName: String): Bitmap {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                if ((i + j) % 2 == 0) {
                    canvas.drawRect(
                        (i * size / 8).toFloat(),
                        (j * size / 8).toFloat(),
                        ((i + 1) * size / 8).toFloat(),
                        ((j + 1) * size / 8).toFloat(),
                        paint
                    )
                }
            }
        }
        
        paint.color = Color.BLUE
        canvas.drawText(tierName, (size / 2).toFloat(), (size / 2).toFloat(), paint)
        canvas.drawText("REWARD QR", (size / 2).toFloat(), (size / 2 + 50).toFloat(), paint)
        
        return bitmap
    }

    fun updatePoints(
        binType: BinType, 
        onComplete: (pointsEarned: Int, newTotal: Int, pointsToNext: Int, streak: Int, isDoubled: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onError("Authentication error: Please sign in again.")
            return
        }

        val userId = user.uid
        val userRef = db.collection("users").document(userId)
        
        userRef.get().addOnSuccessListener { snapshot ->
            val currentPoints = snapshot.getLong("totalPoints")?.toInt() ?: 0
            val lastRecycleTimestamp = snapshot.getTimestamp("lastRecycleDate")
            var currentStreak = snapshot.getLong("streakCount")?.toInt() ?: 0
            val now = Timestamp.now()

            var isDoubled = false
            if (lastRecycleTimestamp != null) {
                if (isConsecutiveDay(lastRecycleTimestamp, now)) {
                    currentStreak++
                } else if (!isSameDay(lastRecycleTimestamp, now)) {
                    currentStreak = 1
                }
            } else {
                currentStreak = 1
            }

            var pointsToAdd = getPointsForBin(binType)
            if (currentStreak >= 5) {
                pointsToAdd *= 2
                isDoubled = true
            }

            val newPoints = currentPoints + pointsToAdd
            val tier = calculateUserTier(newPoints)
            
            val pointsToNext = when(tier.levelNumber) {
                1 -> 151 - newPoints
                2 -> 501 - newPoints
                else -> 0
            }
            
            val data = mutableMapOf(
                "totalPoints" to newPoints,
                "level" to tier.name,
                "lastRecycleDate" to now,
                "streakCount" to currentStreak,
                "displayName" to (user.displayName ?: user.email?.substringBefore("@") ?: "User"),
                "email" to user.email
            )
            
            userRef.set(data, SetOptions.merge())
                .addOnSuccessListener {
                    onComplete(pointsToAdd, newPoints, pointsToNext, currentStreak, isDoubled)
                }
                .addOnFailureListener { e ->
                    Log.e("RewardManager", "Update failed", e)
                    onError("Update failed: ${e.localizedMessage}")
                }
        }.addOnFailureListener { e ->
            Log.e("RewardManager", "Get failed", e)
            onError("Data access failed: ${e.localizedMessage}")
        }
    }

    private fun isConsecutiveDay(last: Timestamp, current: Timestamp): Boolean {
        val lastCal = Calendar.getInstance().apply { time = last.toDate() }
        val currentCal = Calendar.getInstance().apply { time = current.toDate() }
        lastCal.add(Calendar.DAY_OF_YEAR, 1)
        return lastCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
               lastCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameDay(last: Timestamp, current: Timestamp): Boolean {
        val lastCal = Calendar.getInstance().apply { time = last.toDate() }
        val currentCal = Calendar.getInstance().apply { time = current.toDate() }
        return lastCal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR) &&
               lastCal.get(Calendar.DAY_OF_YEAR) == currentCal.get(Calendar.DAY_OF_YEAR)
    }
}
