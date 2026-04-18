package com.example.kotlin_project_1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class MainActivity : AppCompatActivity() {
    private val tag = "btaMainActivity"

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w(tag, "Google sign in failed", e)
                    showToast("Google sign in failed")
                }
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val switchLocation: SwitchCompat = findViewById(R.id.switchLocation)
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            
            if (granted) {
                Log.d(tag, "Location permissions granted via switch")
                showToast("Location Permissions Granted")
                switchLocation.isChecked = true
            } else {
                Log.w(tag, "Location permissions denied via switch")
                showToast("Location Permissions Denied")
                switchLocation.isChecked = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate: The activity is being created.")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Firebase Auth & Firestore
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        // Auth Logic
        val btnGoogleSignIn: Button = findViewById(R.id.btnGoogleSignIn)
        btnGoogleSignIn.setOnClickListener {
            signIn()
        }

        val btnSignOut: Button = findViewById(R.id.btnSignOut)
        btnSignOut.setOnClickListener {
            signOut()
        }

        // Enable Location Switch Logic
        val switchLocation: SwitchCompat = findViewById(R.id.switchLocation)
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        switchLocation.isChecked = hasPermission

        switchLocation.setOnClickListener {
            val isChecked = switchLocation.isChecked
            if (isChecked) {
                Log.d(tag, "Attempting to turn location ON")
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                showRevokePermissionDialog()
                switchLocation.isChecked = true 
            }
        }

        // Quick Actions Grid Click Listeners
        findViewById<MaterialCardView>(R.id.cardMap).setOnClickListener {
            startActivity(Intent(this, OpenStreetMapsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardWeather).setOnClickListener {
            startActivity(Intent(this, WeatherActivity::class.java))
        }

        // Ranking Card Click Listener
        findViewById<MaterialCardView>(R.id.cardRanking).setOnClickListener {
            startActivity(Intent(this, RankingActivity::class.java))
        }

        setupBottomNavigation()
        updateUI()

        // Handle back pressed
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userRef = db.collection("users").document(it.uid)
                        val data = mapOf(
                            "displayName" to it.displayName,
                            "email" to it.email
                        )
                        // Use merge to not overwrite points if they already exist
                        userRef.set(data, SetOptions.merge()).addOnSuccessListener { 
                            updateUI() 
                        }
                    }
                } else {
                    Log.w(tag, "signInWithCredential:failure", task.exception)
                    showToast("Authentication Failed.")
                }
            }
    }

    private fun signOut() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(this) {
            updateUI()
        }
    }

    private fun updateUI() {
        val user = auth.currentUser
        val tvUserStatus: TextView = findViewById(R.id.tvUserStatus)
        val tvStreakCount: TextView = findViewById(R.id.tvStreakCount)
        val tvUniversityRank: TextView = findViewById(R.id.tvUniversityRank)

        if (user != null) {
            tvUserStatus.text = getString(R.string.logged_in_as, user.email)
            findViewById<Button>(R.id.btnGoogleSignIn).visibility = android.view.View.GONE
            findViewById<Button>(R.id.btnSignOut).visibility = android.view.View.VISIBLE
            
            // Fetch User Data for Streak and Rank
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val streak = document.getLong("streakCount") ?: 0
                    tvStreakCount.text = "$streak Days"
                    calculateRank(user.uid)
                } else {
                    tvStreakCount.text = "0 Days"
                    tvUniversityRank.text = "--"
                    // Initialize document if it doesn't exist
                    val data = mapOf(
                        "totalPoints" to 0,
                        "level" to "Eco-Conscious",
                        "streakCount" to 0,
                        "displayName" to user.displayName,
                        "email" to user.email
                    )
                    db.collection("users").document(user.uid).set(data)
                }
            }
        } else {
            tvUserStatus.text = getString(R.string.not_logged_in)
            tvStreakCount.text = "0 Days"
            tvUniversityRank.text = "--"
            findViewById<Button>(R.id.btnGoogleSignIn).visibility = android.view.View.VISIBLE
            findViewById<Button>(R.id.btnSignOut).visibility = android.view.View.GONE
        }
    }

    private fun calculateRank(userId: String) {
        db.collection("users")
            .orderBy("totalPoints", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                var rank = 1
                var found = false
                for (doc in documents) {
                    if (doc.id == userId) {
                        findViewById<TextView>(R.id.tvUniversityRank).text = "#$rank"
                        found = true
                        break
                    }
                    rank++
                }
                if (!found) {
                    findViewById<TextView>(R.id.tvUniversityRank).text = "--"
                }
            }
            .addOnFailureListener { e ->
                Log.e(tag, "Error calculating rank", e)
                findViewById<TextView>(R.id.tvUniversityRank).text = "Error"
            }
    }

    private fun handleNavigation(itemId: Int) {
        when (itemId) {
            R.id.nav_home -> { }
            R.id.nav_map -> startActivity(Intent(this, OpenStreetMapsActivity::class.java))
            R.id.nav_list -> startActivity(Intent(this, MainActivity2::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            handleNavigation(item.itemId)
            true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showRevokePermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Revoke Permission")
            .setMessage("To revoke permissions, you must do it in the system settings. Open settings now?")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onResume() {
        super.onResume()
        val switchLocation: SwitchCompat = findViewById(R.id.switchLocation)
        val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        switchLocation.isChecked = hasPermission
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_home
        updateUI()
    }
}