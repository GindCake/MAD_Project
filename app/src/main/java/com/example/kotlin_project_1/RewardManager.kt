package com.example.kotlin_project_1

import android.graphics.Bitmap
import android.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

data class UserTier(
    val name: String,
    val discount: String,
    val minPoints: Int,
    val maxPoints: Int,
    val levelNumber: Int
)

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

    fun updatePoints(binType: BinType, onComplete: (pointsEarned: Int, newTotal: Int, pointsToNext: Int) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val pointsToAdd = getPointsForBin(binType)

        val userRef = db.collection("users").document(userId)
        
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentPoints = snapshot.getLong("totalPoints")?.toInt() ?: 0
            val newPoints = currentPoints + pointsToAdd
            
            val tier = calculateUserTier(newPoints)
            
            val pointsToNext = when(tier.levelNumber) {
                1 -> 151 - newPoints
                2 -> 501 - newPoints
                else -> 0
            }
            
            val data = mapOf(
                "totalPoints" to newPoints,
                "level" to tier.name,
                "discount" to "${tier.discount} Discount"
            )
            
            transaction.set(userRef, data, SetOptions.merge())
            Triple(pointsToAdd, newPoints, pointsToNext)
        }.addOnSuccessListener { result ->
            onComplete(result.first, result.second, result.third)
        }
    }

    fun generateQRCodePlaceholder(levelName: String): Bitmap {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val color = when (levelName) {
            "Eco-Conscious" -> Color.parseColor("#4CAF50")
            "Eco-Warrior" -> Color.parseColor("#2196F3")
            "Planet Guardian" -> Color.parseColor("#FF9800")
            else -> Color.BLACK
        }
        
        for (x in 0 until size) {
            for (y in 0 until size) {
                val isPattern = (x / 32 + y / 32) % 2 == 0
                bitmap.setPixel(x, y, if (isPattern) color else Color.WHITE)
            }
        }
        return bitmap
    }
}