package com.example.villactiva

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.villactiva.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var loggedInUser: String

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el usuario logueado
        loggedInUser = obtenerUsuarioLogueado()

        if (loggedInUser.isEmpty()) {
            // Si no hay usuario logueado, redirigir al com.example.villactiva.LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setupBottomNavigation()

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
        }
    }

    private fun obtenerUsuarioLogueado(): String {
        val sharedPreferences = getSharedPreferences("VillactivaPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("logged_in_user", "") ?: ""
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_reservations -> {
                    val fragment = ReservationsFragment.newInstance(loggedInUser)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit()
                    true
                }
                R.id.nav_logout -> {
                    cerrarSesion()
                    true
                }
                else -> false
            }
        }
    }

    fun cerrarSesion() {
        // Eliminar el usuario logueado de SharedPreferences
        val sharedPreferences = getSharedPreferences("VillactivaPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }

        // Cerrar sesión y redirigir al com.example.villactiva.LoginActivity
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
