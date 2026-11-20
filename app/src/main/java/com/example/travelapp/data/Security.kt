package com.example.travelapp.data

import java.security.MessageDigest

// Превращает пароль в «отпечаток» (SHA-256), чтобы в базе не лежал сам пароль
fun hashPassword(email: String, password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256")
        .digest("$email:$password".toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}