package com.example.villactiva

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.villactiva.databinding.DetailActivityBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: DetailActivityBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var loggedInUser: String

    private var idPlace: String = ""
    private var selectedStartDate: Long = 0L
    private var selectedEndDate: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el usuario logueado desde SharedPreferences
        val sharedPreferences = getSharedPreferences("VillactivaPrefs", Context.MODE_PRIVATE)
        loggedInUser = sharedPreferences.getString("logged_in_user", "") ?: ""
        if (loggedInUser.isEmpty()) {
            Toast.makeText(this, "Usuario no identificado. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Obtener datos pasados por el intent
        val name = intent.getStringExtra("PLACE_NAME") ?: ""
        val description = intent.getStringExtra("PLACE_DESCRIPTION") ?: ""
        val image = intent.getStringExtra("PLACE_IMAGE") ?: ""
        idPlace = intent.getStringExtra("PLACE_ID") ?: ""

        // Mostrar datos en la pantalla
        binding.tvDetailName.text = name
        binding.tvDetailDescription.text = description
        Glide.with(this).load(image).into(binding.ivDetailImage)

        // Configurar el botón de hacer reserva
        binding.btnMakeReservation.setOnClickListener {
            openDatePicker()
        }

        // Ocultar el botón de guardar al inicio
        binding.btnSaveReservation.visibility = View.GONE

        // Configurar el botón de guardar reserva
        binding.btnSaveReservation.setOnClickListener {
            saveReservation()
        }
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        selectedStartDate = calendar.timeInMillis
                        selectedEndDate = selectedStartDate + 3600000 // Sumar 1 hora

                        validateDates()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Restringir fechas pasadas
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun validateDates() {
        // Consultar todas las reservas para el idPlace
        firestore.collection("Reservations")
            .whereEqualTo("idPlace", idPlace)
            .get()
            .addOnSuccessListener { snapshot ->
                var isValid = true

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedStartDate

                // Validar fines de semana
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    isValid = false
                    Toast.makeText(this, "No se permiten reservas los fines de semana.", Toast.LENGTH_SHORT).show()
                }

                // Validar horario permitido
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                if (hour < 9 || hour >= 21) {
                    isValid = false
                    Toast.makeText(this, "Las reservas deben estar entre las 9:00 y las 21:00.", Toast.LENGTH_SHORT).show()
                }

                // Validar fechas en el pasado
                if (selectedStartDate < System.currentTimeMillis()) {
                    isValid = false
                    Toast.makeText(this, "No puedes seleccionar fechas en el pasado.", Toast.LENGTH_SHORT).show()
                }

                // Validar duración mínima de 1 hora
                if (selectedEndDate <= selectedStartDate) {
                    isValid = false
                    Toast.makeText(this, "La reserva debe durar al menos 1 hora.", Toast.LENGTH_SHORT).show()
                }

                // Validar conflictos con otras reservas
                if (isValid) {
                    for (document in snapshot) {
                        val existingStart = document.getTimestamp("dateStart")?.toDate()?.time ?: 0L
                        val existingEnd = document.getTimestamp("dateEnd")?.toDate()?.time ?: 0L

                        if (!(selectedEndDate <= existingStart || selectedStartDate >= existingEnd)) {
                            isValid = false
                            Toast.makeText(this, "Esta franja horaria ya está reservada.", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }
                }

                // Si las fechas son válidas, mostrar el botón de guardar
                if (isValid) {
                    binding.btnSaveReservation.visibility = View.VISIBLE
                    Toast.makeText(this, "Fechas válidas. Ahora puedes guardar la reserva.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.btnSaveReservation.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al validar las fechas.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveReservation() {
        val image = intent.getStringExtra("PLACE_IMAGE") ?: ""

        val reservationData = hashMapOf(
            "name" to binding.tvDetailName.text.toString(),
            "description" to binding.tvDetailDescription.text.toString(),
            "image" to image,
            "dateStart" to Timestamp(Date(selectedStartDate)),
            "dateEnd" to Timestamp(Date(selectedEndDate)),
            "idPlace" to idPlace,
            "userId" to loggedInUser
        )

        // Generar un ID único para la reserva
        val reservationId = firestore.collection("Reservations").document().id

        // Guardar en la colección global de reservas
        firestore.collection("Reservations")
            .document(reservationId)
            .set(reservationData)
            .addOnSuccessListener {
                // Guardar en la subcolección de reservas del usuario
                firestore.collection("User")
                    .document(loggedInUser)
                    .collection("Reservations")
                    .document(reservationId)
                    .set(reservationData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Reserva guardada con éxito.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al guardar la reserva para el usuario: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar la reserva: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}