package com.example.kotlin_project_1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {
    private val tag = "btaSettingsActivity"
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val rewardManager = RewardManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate: Settings activity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_home, R.string.nav_home)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { item ->
            handleNavigation(item.itemId)
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content)) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        findViewById<ImageButton>(R.id.btnInfo).setOnClickListener {
            RewardsGuideBottomSheet().show(supportFragmentManager, RewardsGuideBottomSheet.TAG)
        }

        setupBottomNavigation()
        fetchUserData()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        val tvUserEmail: TextView = findViewById(R.id.tvUserEmail)
        val tvPoints: TextView = findViewById(R.id.tvPoints)
        val tvLevel: TextView = findViewById(R.id.tvLevel)
        val tvDiscount: TextView = findViewById(R.id.tvDiscount)
        val ivQRCode: ImageView = findViewById(R.id.ivQRCode)
        val pbLevelProgress: ProgressBar = findViewById(R.id.pbLevelProgress)
        val tvNextLevelInfo: TextView = findViewById(R.id.tvNextLevelInfo)

        tvUserEmail.text = getString(R.string.logged_in_as, auth.currentUser?.email)

        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(tag, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val points = snapshot.getLong("totalPoints")?.toInt() ?: 0
                    val tier = rewardManager.calculateUserTier(points)

                    tvPoints.text = "Total Points: $points"
                    tvLevel.text = "Level: ${tier.name}"
                    tvDiscount.text = "Status: ${tier.discount} Discount"
                    
                    pbLevelProgress.max = tier.maxPoints
                    pbLevelProgress.progress = points
                    
                    val pointsToNext = when(tier.levelNumber) {
                        1 -> 151 - points
                        2 -> 501 - points
                        else -> 0
                    }

                    tvNextLevelInfo.text = if (tier.levelNumber < 3) {
                        "$pointsToNext pts to next level"
                    } else {
                        "Max level reached!"
                    }
                    
                    // Use the pattern placeholder since ZXing was removed
                    ivQRCode.setImageBitmap(rewardManager.generateQRCodePlaceholder(tier.name))
                } else {
                    tvPoints.text = "Total Points: 0"
                    tvNextLevelInfo.text = "151 pts to next level"
                    pbLevelProgress.max = 150
                    pbLevelProgress.progress = 0
                }
            }
    }

    private fun handleNavigation(itemId: Int) {
        when (itemId) {
            R.id.nav_home -> startActivity(Intent(this, MainActivity::class.java))
            R.id.nav_map -> startActivity(Intent(this, OpenStreetMapsActivity::class.java))
            R.id.nav_list -> startActivity(Intent(this, MainActivity2::class.java))
            R.id.nav_settings -> { /* Already here */ }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_settings
        bottomNavigation.setOnItemSelectedListener { item ->
            handleNavigation(item.itemId)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_settings
    }
}