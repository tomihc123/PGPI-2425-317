package com.example.villactiva.model

import com.google.firebase.Timestamp

data class Reservation(
    var id: String = "", // Campo para el ID del documento Firestore
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val dateStart: Timestamp? = null, // Cambiado a Timestamp
    val dateEnd: Timestamp? = null,   // Cambiado a Timestamp
    val idPlace: String = ""
) {
    // Métodos de utilidad para obtener los valores como Long
    fun getDateStartMillis(): Long = dateStart?.toDate()?.time ?: 0L
    fun getDateEndMillis(): Long = dateEnd?.toDate()?.time ?: 0L
}

