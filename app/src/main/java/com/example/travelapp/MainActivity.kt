package com.example.travelapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupBottomNav()
        goTo(R.id.tvAllCities, CitiesActivity::class.java)
        goTo(R.id.btnPlanTrip, ActivityPlanning1::class.java)
        goTo(R.id.btnWeekRoute, ActivityRouteMap::class.java)

        // Пхукет открывает страницу города
        goTo(R.id.cardPhuket, ActivityCityPhuket::class.java)

        // Остальные города пока недоступны
        val soon = View.OnClickListener {
            Toast.makeText(this, "Этот город скоро появится", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.cardBali).setOnClickListener(soon)
        findViewById<View>(R.id.cardAntalya).setOnClickListener(soon)
    }
}