package com.example.travelapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.travelapp.data.AppDatabase
import com.example.travelapp.data.Session
import com.example.travelapp.data.hashPassword
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupBottomNav()
        goTo(R.id.tvRegister, RegisterActivity::class.java)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = etEmail.text.toString().trim().lowercase()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Введите email и пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = AppDatabase.get(this@LoginActivity).userDao()
                    .login(email, hashPassword(email, password))
                if (user == null) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Неверный email или пароль",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Session.login(this@LoginActivity, user.id)
                    startActivity(Intent(this@LoginActivity, ActivityProfile::class.java))
                    finish()
                }
            }
        }
    }
}