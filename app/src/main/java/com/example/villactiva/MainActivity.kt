package com.example.villactiva

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.villactiva.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSearch.setOnClickListener {
            val dni = binding.etDni.text.toString().trim()
            if (dni.isNotEmpty()) {
                fetchUserData(dni)
            } else {
                Toast.makeText(this, "Por favor ingrese un DNI", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserData(dni: String) {
        binding.tvUserName.text = ""
        binding.llReservations.removeAllViews()

        db.collection("User").document(dni).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name")
                    binding.tvUserName.text = "Nombre: $name"

                    db.collection("User").document(dni).collection("Reservations")
                        .get()
                        .addOnSuccessListener { reservationSnapshot ->
                            if (!reservationSnapshot.isEmpty) {
                                // Recorre y muestra cada reserva
                                for (reservation in reservationSnapshot) {
                                    val reservationName = reservation.getString("name")
                                    val textView = TextView(this)
                                    textView.text = "- $reservationName"
                                    binding.llReservations.addView(textView)
                                }
                            } else {
                                Toast.makeText(this, "No tiene reservas", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                this,
                                "Error al obtener reservas: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error al obtener datos: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
