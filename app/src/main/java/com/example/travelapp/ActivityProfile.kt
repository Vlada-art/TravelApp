package com.example.travelapp

import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.travelapp.data.AppDatabase
import com.example.travelapp.data.Session
import com.example.travelapp.data.User
import com.example.travelapp.data.hashPassword
import kotlinx.coroutines.launch

class ActivityProfile : AppCompatActivity() {

    private var currentUser: User? = null
    private val db by lazy { AppDatabase.get(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupBottomNav()
        goTo(R.id.itemSaved, ActivitySaved::class.java)

        // Если никто не вошёл, отправляем на экран входа
        if (Session.userId(this) == -1L) {
            openLogin()
            return
        }

        findViewById<View>(R.id.btnEditProfile).setOnClickListener { showEditDialog() }
        findViewById<View>(R.id.btnLogout).setOnClickListener {
            Session.logout(this)
            openLogin()
        }
        findViewById<View>(R.id.btnDeleteAccount).setOnClickListener { showDeleteDialog() }

        loadUser()
    }

    // Читаем пользователя из базы и показываем на экране
    private fun loadUser() {
        lifecycleScope.launch {
            val user = db.userDao().getById(Session.userId(this@ActivityProfile))
            if (user == null) {
                Session.logout(this@ActivityProfile)
                openLogin()
                return@launch
            }
            currentUser = user
            findViewById<TextView>(R.id.tvProfileName).text = user.name
            findViewById<TextView>(R.id.tvProfileEmail).text = user.email
        }
    }

    // Изменение данных (Update)
    private fun showEditDialog() {
        val user = currentUser ?: return
        val pad = (20 * resources.displayMetrics.density).toInt()

        val etName = EditText(this).apply {
            hint = "Имя"
            setText(user.name)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }
        val etPassword = EditText(this).apply {
            hint = "Новый пароль (необязательно)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
            addView(etName)
            addView(etPassword)
        }

        AlertDialog.Builder(this)
            .setTitle("Изменить данные")
            .setView(form)
            .setPositiveButton("Сохранить") { _, _ ->
                val newName = etName.text.toString().trim()
                val newPassword = etPassword.text.toString()
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Имя не может быть пустым", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (newPassword.isNotEmpty() && newPassword.length < 6) {
                    Toast.makeText(this, "Пароль должен быть не короче 6 символов", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val updated = user.copy(
                    name = newName,
                    passwordHash = if (newPassword.isEmpty()) user.passwordHash
                    else hashPassword(user.email, newPassword)
                )
                lifecycleScope.launch {
                    db.userDao().update(updated)
                    Toast.makeText(this@ActivityProfile, "Данные обновлены", Toast.LENGTH_SHORT).show()
                    loadUser()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Удаление аккаунта (Delete)
    private fun showDeleteDialog() {
        val user = currentUser ?: return
        AlertDialog.Builder(this)
            .setTitle("Удалить аккаунт?")
            .setMessage("Аккаунт и всё избранное будут удалены без возможности восстановления.")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    db.favoriteDao().deleteAllForUser(user.id)
                    db.userDao().delete(user)
                    Session.logout(this@ActivityProfile)
                    Toast.makeText(this@ActivityProfile, "Аккаунт удалён", Toast.LENGTH_SHORT).show()
                    openLogin()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}