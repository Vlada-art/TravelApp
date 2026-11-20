package com.example.travelapp

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.travelapp.data.AppDatabase
import com.example.travelapp.data.Session
import com.example.travelapp.data.User
import com.example.travelapp.data.hashPassword
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupBottomNav()
        goTo(R.id.tvLogin, LoginActivity::class.java)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase()
            val password = etPassword.text.toString()

            if (name.isEmpty()) {
                etName.error = "Введите имя"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Введите корректный email"
                return@setOnClickListener
            }
            if (password.length < 6) {
                etPassword.error = "Пароль должен быть не короче 6 символов"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val newId = AppDatabase.get(this@RegisterActivity).userDao().insert(
                        User(
                            name = name,
                            email = email,
                            passwordHash = hashPassword(email, password)
                        )
                    )
                    Session.login(this@RegisterActivity, newId)
                    Toast.makeText(
                        this@RegisterActivity,
                        "Регистрация прошла успешно",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@RegisterActivity, ActivityProfile::class.java))
                    finish()
                } catch (e: SQLiteConstraintException) {
                    etEmail.error = "Этот email уже зарегистрирован"
                }
            }
        }
    }
}