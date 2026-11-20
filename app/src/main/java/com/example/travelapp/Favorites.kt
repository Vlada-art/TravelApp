package com.example.travelapp

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import com.example.travelapp.data.AppDatabase
import com.example.travelapp.data.Favorite
import com.example.travelapp.data.Session
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch

// Каталог мест: к какой категории относится место и какая у него картинка.
// Типы: "hotel" (отели), "restaurant" (рестораны), "place" (пляжи и развлечения)
object PlaceCatalog {
    private class Item(val type: String, val nameRes: Int, val imageRes: Int)

    private val items = listOf(
        // ----- Отели -----
        Item("hotel", R.string.h1, R.drawable.hotel_1),
        Item("hotel", R.string.h2, R.drawable.hotel_2),
        Item("hotel", R.string.h3, R.drawable.hotel_3),
        Item("hotel", R.string.h4, R.drawable.hotel_3),
        Item("hotel", R.string.h5, R.drawable.hotel_2),
        Item("hotel", R.string.h6, R.drawable.hotel_1),
        Item("hotel", R.string.h7, R.drawable.hotel_1),
        Item("hotel", R.string.h8, R.drawable.hotel_3),
        Item("hotel", R.string.h9, R.drawable.hotel_2),
        Item("hotel", R.string.h10, R.drawable.hotel_2),
        Item("hotel", R.string.h11, R.drawable.hotel_3),
        Item("hotel", R.string.h12, R.drawable.hotel_1),
        Item("hotel", R.string.h13, R.drawable.hotel_3),
        Item("hotel", R.string.h14, R.drawable.hotel_2),

        // ----- Рестораны -----
        Item("restaurant", R.string.r1, R.drawable.restoraunt_1),
        Item("restaurant", R.string.r2, R.drawable.restoraunt_2),
        Item("restaurant", R.string.r3, R.drawable.restoraunt_3),
        Item("restaurant", R.string.r4, R.drawable.restoraunt_3),
        Item("restaurant", R.string.r5, R.drawable.restoraunt_1),
        Item("restaurant", R.string.r6, R.drawable.restoraunt_2),
        Item("restaurant", R.string.r7, R.drawable.restoraunt_2),
        Item("restaurant", R.string.r8, R.drawable.restoraunt_3),
        Item("restaurant", R.string.r9, R.drawable.restoraunt_1),
        Item("restaurant", R.string.r10, R.drawable.restoraunt_1),
        Item("restaurant", R.string.r11, R.drawable.restoraunt_2),
        Item("restaurant", R.string.r12, R.drawable.restoraunt_3),

        // ----- Места: пляжи -----
        Item("place", R.string.b1, R.drawable.beach01),
        Item("place", R.string.b2, R.drawable.beach02),
        Item("place", R.string.b3, R.drawable.beach03),
        Item("place", R.string.b4, R.drawable.beach03),
        Item("place", R.string.b5, R.drawable.beach01),
        Item("place", R.string.b6, R.drawable.beach02),
        Item("place", R.string.b7, R.drawable.beach02),
        Item("place", R.string.b8, R.drawable.beach03),
        Item("place", R.string.b9, R.drawable.beach01),
        Item("place", R.string.b10, R.drawable.beach01),
        Item("place", R.string.b11, R.drawable.beach02),
        Item("place", R.string.b12, R.drawable.beach03),

        // ----- Места: развлечения -----
        Item("place", R.string.e1, R.drawable.thai_sign_6),
        Item("place", R.string.e2, R.drawable.indian_sign_4),
        Item("place", R.string.e3, R.drawable.thai_beach_sign_5),
        Item("place", R.string.e4, R.drawable.europe_sign_3),
        Item("place", R.string.e5, R.drawable.shanhai_sign_2),
        Item("place", R.string.e6, R.drawable.restoraunt_1),
        Item("place", R.string.e7, R.drawable.egypt_sign_1),
        Item("place", R.string.e8, R.drawable.beach01),
        Item("place", R.string.e9, R.drawable.restoraunt_2),
        Item("place", R.string.e10, R.drawable.beach02),
        Item("place", R.string.e11, R.drawable.beach03),
        Item("place", R.string.e12, R.drawable.restoraunt_3),

        // ----- Подборки на странице Пхукета -----
        Item("restaurant", R.string.place_dinner_sky, R.drawable.restoraunt_1),
        Item("restaurant", R.string.place_blanket, R.drawable.restoraunt_2),
        Item("restaurant", R.string.place_siam, R.drawable.restoraunt_3),
        Item("hotel", R.string.hotel_movenpick, R.drawable.hotel_1),
        Item("hotel", R.string.hotel_royal, R.drawable.hotel_2),
        Item("hotel", R.string.hotel_seascape, R.drawable.hotel_3),
        Item("place", R.string.beach_paradise, R.drawable.beach01),
        Item("place", R.string.beach_freedom, R.drawable.beach02),
        Item("place", R.string.beach_surin, R.drawable.beach03)
    )

    private fun find(context: Context, name: String): Item? =
        items.firstOrNull { context.getString(it.nameRes) == name }

    // Категория места по названию (или null, если места нет в каталоге)
    fun typeFor(context: Context, name: String): String? = find(context, name)?.type

    // Картинка места по названию (у маршрута картинка — карта)
    fun imageFor(context: Context, type: String, name: String): Int =
        if (type == "route") R.drawable.phuket_map
        else find(context, name)?.imageRes ?: R.drawable.hotel_1
}

// Делает все сердечки на экране рабочими.
// defaultType используется, только если места нет в каталоге.
fun AppCompatActivity.setupFavoriteHearts(defaultType: String = "place") {
    val userId = Session.userId(this)
    val dao = AppDatabase.get(this).favoriteDao()
    val outlineRes = styleSrc(R.style.CardHeart)
    val filledRes = styleSrc(R.style.CardHeartFilled)

    // Находим все карточки: «рамка» с сердечком и названием внутри
    val cards = mutableListOf<Pair<ImageView, TextView>>()
    collectCards(findViewById<View>(R.id.main), cards)

    fun showState(heart: ImageView, isFavorite: Boolean) {
        if (outlineRes != 0 && filledRes != 0) {
            heart.setImageResource(if (isFavorite) filledRes else outlineRes)
        } else {
            heart.alpha = if (isFavorite) 1f else 0.5f
        }
    }

    // Обновляет все сердечки с таким названием (одно место может встречаться на экране дважды)
    fun showStateByName(name: String, isFavorite: Boolean) {
        cards.filter { it.second.text.toString() == name }
            .forEach { showState(it.first, isFavorite) }
    }

    lifecycleScope.launch {
        val saved: Set<String> = if (userId == -1L) emptySet()
        else dao.getAll(userId).map { it.placeName }.toSet()

        cards.forEach { (heart, title) ->
            val name = title.text.toString()
            showState(heart, name in saved)

            heart.setOnClickListener {
                if (userId == -1L) {
                    Toast.makeText(
                        this@setupFavoriteHearts,
                        "Войдите в аккаунт, чтобы сохранять места",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                lifecycleScope.launch {
                    if (dao.isFavorite(userId, name) > 0) {
                        dao.deleteByName(userId, name)
                        showStateByName(name, false)
                    } else {
                        val type = PlaceCatalog.typeFor(this@setupFavoriteHearts, name) ?: defaultType
                        dao.insert(Favorite(userId = userId, placeType = type, placeName = name))
                        showStateByName(name, true)
                    }
                }
            }
        }
    }
}

private fun collectCards(view: View, out: MutableList<Pair<ImageView, TextView>>) {
    if (view is FrameLayout) {
        val heart = view.children.firstOrNull { it is ImageView && it !is ShapeableImageView } as? ImageView
        val title = view.children.firstOrNull { it is TextView } as? TextView
        if (heart != null && title != null) {
            out.add(heart to title)
            return
        }
    }
    if (view is ViewGroup) {
        view.children.forEach { collectCards(it, out) }
    }
}

// Достаём из стиля картинку сердечка (пустое / закрашенное)
private fun Context.styleSrc(style: Int): Int {
    val a = obtainStyledAttributes(style, intArrayOf(android.R.attr.src))
    val id = a.getResourceId(0, 0)
    a.recycle()
    return id
}