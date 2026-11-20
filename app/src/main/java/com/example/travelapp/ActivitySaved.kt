package com.example.travelapp

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.travelapp.data.AppDatabase
import com.example.travelapp.data.Favorite
import com.example.travelapp.data.Session
import kotlinx.coroutines.launch

class ActivitySaved : AppCompatActivity() {

    // Выбранная категория: null = «Все», иначе "hotel", "restaurant", "place", "route"
    private var currentFilter: String? = null

    private lateinit var chips: List<Pair<TextView, String?>>
    private lateinit var activeBg: Drawable
    private lateinit var inactiveBg: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_saved)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupBottomNav()

        // Если экран открыли с готовым фильтром (например, после сохранения маршрута)
        currentFilter = intent.getStringExtra("filter")
        setupFilters()
    }

    // Плашки-фильтры
    private fun setupFilters() {
        val chipAll = findViewById<TextView>(R.id.chipAll)
        val chipHotels = findViewById<TextView>(R.id.chipHotels)
        val chipRestaurants = findViewById<TextView>(R.id.chipRestaurants)
        val chipPlaces = findViewById<TextView>(R.id.chipPlaces)
        val chipRoutes = findViewById<TextView>(R.id.chipRoutes)

        // Запоминаем, как выглядит выбранная и невыбранная плашка
        activeBg = chipAll.background
        inactiveBg = chipHotels.background

        chips = listOf(
            chipAll to null,
            chipHotels to "hotel",
            chipRestaurants to "restaurant",
            chipPlaces to "place",
            chipRoutes to "route"
        )

        chips.forEach { (chip, type) ->
            chip.setOnClickListener {
                currentFilter = type
                updateChips()
                loadFavorites()
            }
        }
        updateChips()
    }

    private fun updateChips() {
        chips.forEach { (chip, type) ->
            val template = if (type == currentFilter) activeBg else inactiveBg
            chip.background = template.constantState?.newDrawable()?.mutate() ?: template
        }
    }

    // Каждый раз, когда экран открывается, читаем избранное из базы
    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        val container = findViewById<LinearLayout>(R.id.favContainer)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val userId = Session.userId(this)
        val filter = currentFilter

        lifecycleScope.launch {
            val all: List<Favorite> = if (userId == -1L) emptyList()
            else AppDatabase.get(this@ActivitySaved).favoriteDao().getAll(userId)

            val favorites = if (filter == null) all else all.filter { it.placeType == filter }

            container.removeAllViews()
            tvEmpty.text = if (all.isEmpty()) "Пока ничего не сохранено"
            else "В этой категории пока ничего нет"
            tvEmpty.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE

            val margin = (4 * resources.displayMetrics.density).toInt()

            // Раскладываем карточки по рядам: по 3 в ряд
            favorites.chunked(3).forEach { rowItems ->
                val row = LinearLayout(this@ActivitySaved)
                row.orientation = LinearLayout.HORIZONTAL

                rowItems.forEach { fav -> row.addView(createCard(row, fav)) }

                // Пустые места, чтобы карточки в неполном ряду были той же ширины
                repeat(3 - rowItems.size) {
                    val spacer = View(this@ActivitySaved)
                    val params = LinearLayout.LayoutParams(0, 1, 1f)
                    params.setMargins(margin, margin, margin, margin)
                    spacer.layoutParams = params
                    row.addView(spacer)
                }

                container.addView(
                    row,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }
    }

    private fun createCard(row: LinearLayout, fav: Favorite): View {
        val card = layoutInflater.inflate(R.layout.item_favorite_card, row, false)
        card.findViewById<ImageView>(R.id.imgPlace)
            .setImageResource(PlaceCatalog.imageFor(this, fav.placeType, fav.placeName))
        card.findViewById<TextView>(R.id.tvPlaceName).text = fav.placeName

        // Нажатие на карточку маршрута открывает карту и расписание (Read)
        if (fav.placeType == "route") {
            card.setOnClickListener {
                startActivity(Intent(this, ActivityRouteMap::class.java))
            }
        }

        // Нажатие на сердечко убирает карточку из избранного (Delete)
        card.findViewById<View>(R.id.btnHeart).setOnClickListener {
            lifecycleScope.launch {
                AppDatabase.get(this@ActivitySaved).favoriteDao()
                    .deleteByName(fav.userId, fav.placeName)
                loadFavorites()
            }
        }
        return card
    }
}