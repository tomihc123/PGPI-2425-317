package com.example.villactiva

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.example.villactiva.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    var firestore = FirebaseFirestore.getInstance()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si hay un usuario guardado
        val sharedPreferences = getSharedPreferences("VillactivaPrefs", Context.MODE_PRIVATE)
        val loggedInUser = sharedPreferences.getString("logged_in_user", null)

        if (loggedInUser != null) {
            // Si hay un usuario guardado, navegar directamente al com.example.villactiva.MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val dni = binding.etDni.text.toString().trim()

            if (dni.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce tu DNI", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            verificarUsuario(dni)
        }
    }

    fun verificarUsuario(dni: String) {
        firestore.collection("User").document(dni).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Guardar el usuario en SharedPreferences
                    guardarUsuarioLogueado(dni)

                    // Navegar al com.example.villactiva.MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al verificar usuario: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarUsuarioLogueado(dni: String) {
        val sharedPreferences = getSharedPreferences("VillactivaPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("logged_in_user", dni)
            apply()
        }
    }

    override fun onBackPressed() {}
}
