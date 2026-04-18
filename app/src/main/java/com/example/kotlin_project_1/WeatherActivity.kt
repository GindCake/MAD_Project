package com.example.kotlin_project_1

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherActivity : AppCompatActivity() {
    private val baseUrl = "https://api.openweathermap.org/data/2.5/"
    private val apiKey = "5b1569857a2b597cb0dfbe33eb23a72a" 

    private lateinit var txtCity: TextView
    private lateinit var txtTemp: TextView
    private lateinit var txtDescription: TextView
    private lateinit var imgWeather: ImageView
    private lateinit var tvHumidity: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvAltitude: TextView
    private lateinit var tableForecast: TableLayout

    private var currentLat: Double = 40.4523
    private var currentLon: Double = -3.7261

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        // Initialize UI
        txtCity = findViewById(R.id.txtCity)
        txtTemp = findViewById(R.id.txtTemp)
        txtDescription = findViewById(R.id.txtDescription)
        imgWeather = findViewById(R.id.imgWeatherIcon)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvDateTime = findViewById(R.id.tvDateTime)
        tvAltitude = findViewById(R.id.tvAltitude)
        tableForecast = findViewById(R.id.tableForecast)

        // Get coordinates from intent if available
        currentLat = intent.getDoubleExtra("LATITUDE", 40.4523)
        currentLon = intent.getDoubleExtra("LONGITUDE", -3.7261)
        val altitude = intent.getDoubleExtra("ALTITUDE", 0.0)
        
        if (intent.hasExtra("ALTITUDE")) {
            tvAltitude.text = String.format(Locale.getDefault(), "Altitude: %.2f m", altitude)
        }

        fetchWeather(currentLat, currentLon)
        fetchForecast(currentLat, currentLon)
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeather(lat, lon, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        txtCity.text = it.name
                        txtTemp.text = String.format(Locale.getDefault(), "%s°C", it.main.temp.toString())
                        txtDescription.text = it.weather[0].description.replaceFirstChar { char -> char.uppercase() }
                        tvHumidity.text = String.format(Locale.getDefault(), "Humidity: %d%%", it.main.humidity)
                        
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        tvDateTime.text = String.format(Locale.getDefault(), "Update: %s", sdf.format(Date(it.dt * 1000)))

                        val iconUrl = "https://openweathermap.org/img/wn/${it.weather[0].icon}@4x.png"
                        Glide.with(this@WeatherActivity).load(iconUrl).into(imgWeather)
                    }
                } else {
                    Toast.makeText(this@WeatherActivity, "Weather Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@WeatherActivity, "Network Failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchForecast(lat: Double, lon: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        val call = service.getForecast(lat, lon, apiKey)

        call.enqueue(object : Callback<ForecastResponse> {
            override fun onResponse(call: Call<ForecastResponse>, response: Response<ForecastResponse>) {
                if (response.isSuccessful) {
                    val forecast = response.body()
                    forecast?.let {
                        populateForecastTable(it.list)
                    }
                } else {
                    Toast.makeText(this@WeatherActivity, "Forecast Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Toast.makeText(this@WeatherActivity, "Network Failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateForecastTable(items: List<ForecastItem>) {
        // Clear existing dynamic rows (keep header at index 0)
        val childCount = tableForecast.childCount
        if (childCount > 1) {
            tableForecast.removeViews(1, childCount - 1)
        }

        // The 5-day / 3-hour forecast returns many items. 
        // We filter to show one per day (around midday) to simulate a daily forecast.
        val dailyForecasts = items.filter { it.dtTxt.contains("12:00:00") }

        for (item in dailyForecasts) {
            val row = TableRow(this)
            row.setPadding(0, 8, 0, 8)
            
            // Format Date (show only date part)
            val dateStr = item.dtTxt.take(10)
            
            row.addView(createCell(dateStr))
            row.addView(createCell("${item.main.temp}°C"))
            row.addView(createCell("${item.main.humidity}%"))
            row.addView(createCell("${item.clouds.all}%"))
            row.addView(createCell("${item.wind.speed} m/s"))

            tableForecast.addView(row)
            
            // Add a thin divider line
            val divider = TableRow(this)
            divider.setBackgroundColor(Color.LTGRAY)
            val params = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 1)
            divider.layoutParams = params
            tableForecast.addView(divider)
        }
    }

    private fun createCell(text: String): TextView {
        val tv = TextView(this)
        tv.text = text
        tv.gravity = Gravity.CENTER
        tv.setPadding(8, 8, 8, 8)
        tv.textSize = 12f
        return tv
    }
}