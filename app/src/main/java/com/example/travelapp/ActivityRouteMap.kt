package com.example.travelapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.travelapp.data.AppDatabase
import com.example.travelapp.data.Favorite
import com.example.travelapp.data.Session
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch

class ActivityRouteMap : AppCompatActivity() {

    companion object {
        // Название маршрута, под которым он сохраняется в избранное
        const val ROUTE_NAME = "Маршрут по Пхукету"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_route_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val behavior = BottomSheetBehavior.from(findViewById<View>(R.id.bottomSheet))
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        findViewById<View>(R.id.dragHandle).setOnClickListener {
            behavior.state = if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                BottomSheetBehavior.STATE_COLLAPSED
            } else {
                BottomSheetBehavior.STATE_EXPANDED
            }
        }

        setupBottomNav()

        // Кнопка «Сохранить маршрут»: записывает маршрут в избранное (Create)
        findViewById<View>(R.id.btnSaveRoute).setOnClickListener {
            val userId = Session.userId(this)
            if (userId == -1L) {
                Toast.makeText(this, "Войдите в аккаунт, чтобы сохранить маршрут", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val dao = AppDatabase.get(this@ActivityRouteMap).favoriteDao()
                if (dao.isFavorite(userId, ROUTE_NAME) > 0) {
                    Toast.makeText(this@ActivityRouteMap, "Этот маршрут уже сохранён", Toast.LENGTH_SHORT).show()
                } else {
                    dao.insert(Favorite(userId = userId, placeType = "route", placeName = ROUTE_NAME))
                    Toast.makeText(this@ActivityRouteMap, "Маршрут сохранён в избранное", Toast.LENGTH_SHORT).show()
                    // Открываем «Сохранённые» сразу на вкладке «Маршруты»
                    val intent = Intent(this@ActivityRouteMap, ActivitySaved::class.java)
                    intent.putExtra("filter", "route")
                    startActivity(intent)
                }
            }
        }
    }
}