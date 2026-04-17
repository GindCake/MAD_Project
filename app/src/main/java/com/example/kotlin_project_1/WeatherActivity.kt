package com.example.kotlin_project_1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherActivity : AppCompatActivity() {
    private val TAG = "btaWeatherActivity"
    private val API_KEY = "4c961a81229641e5dfeb04be4f4d5ef6"
    private val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Weather activity created")
        enableEdgeToEdge()
        setContentView(R.layout.activity_weather)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupNavigation()
        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        
        // Default location: Campus Sur UPM (Madrid)
        val lat = 40.3323
        val lon = -3.7653

        service.getCurrentWeather(lat, lon, API_KEY).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    weather?.let {
                        updateUI(it)
                    }
                } else {
                    val errorMsg = "Error ${response.code()}: ${response.message()}"
                    Log.e(TAG, "Response failed: $errorMsg")
                    // If you get 401, your API key is likely not active yet.
                    Toast.makeText(this@WeatherActivity, "Failed to fetch: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e(TAG, "Network failure", t)
                Toast.makeText(this@WeatherActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateUI(weather: WeatherResponse) {
        findViewById<TextView>(R.id.tvLocation).text = "Location: ${weather.name}"
        findViewById<TextView>(R.id.tvTemperature).text = "${weather.main.temp} °C"
        findViewById<TextView>(R.id.tvDescription).text = weather.weather[0].description
        findViewById<TextView>(R.id.tvHumidity).text = "Humidity: ${weather.main.humidity}%"
        Log.d(TAG, "Weather updated: ${weather.name}, ${weather.main.temp}C")
    }

    private fun setupNavigation() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_map // Assuming weather is part of map/location section
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, OpenStreetMapsActivity::class.java))
                    true
                }
                R.id.nav_list -> {
                    startActivity(Intent(this, MainActivity2::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}