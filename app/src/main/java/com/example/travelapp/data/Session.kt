package com.example.travelapp.data

import android.content.Context

// Запоминает номер вошедшего пользователя (даже если приложение закрыли)
object Session {
    private const val PREFS = "session"
    private const val KEY_USER_ID = "user_id"

    fun login(context: Context, userId: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(KEY_USER_ID, userId).apply()
    }

    // Номер вошедшего пользователя или -1, если никто не вошёл
    fun userId(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_USER_ID, -1L)

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_USER_ID).apply()
    }
}