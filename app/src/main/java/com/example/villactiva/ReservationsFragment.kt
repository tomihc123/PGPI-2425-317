package com.example.villactiva

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.villactiva.adapter.GenericSimpleRecyclerBindingInterface
import com.example.villactiva.adapter.SimpleGenericRecyclerAdapter
import com.example.villactiva.databinding.FragmentReservationsBinding
import com.example.villactiva.databinding.ItemReservationBinding
import com.example.villactiva.model.Reservation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ReservationsFragment : Fragment() {
    private lateinit var binding: FragmentReservationsBinding
    private val reservationsList = mutableListOf<Reservation>()
    private val firestore = FirebaseFirestore.getInstance()
    private var loggedInUser: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loggedInUser = arguments?.getString("USER_DNI")
    }

    companion object {
        // Método para crear una nueva instancia del fragmento con argumentos
        fun newInstance(userDni: String): ReservationsFragment {
            val fragment = ReservationsFragment()
            val args = Bundle()
            args.putString("USER_DNI", userDni) // Pasar el DNI como argumento
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReservationsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        fetchReservations()
        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = SimpleGenericRecyclerAdapter(
            dataSet = reservationsList,
            bindingInflater = ItemReservationBinding::inflate,
            bindingInterface = object : GenericSimpleRecyclerBindingInterface<Reservation, ItemReservationBinding> {
                override fun bindData(
                    item: Reservation,
                    binding: ItemReservationBinding,
                    position: Int,
                    adapter: SimpleGenericRecyclerAdapter<Reservation, ItemReservationBinding>
                ) {
                    // Configurar los datos del ítem
                    Glide.with(this@ReservationsFragment)
                        .load(item.image)
                        .into(binding.ivReservationImage)

                    binding.tvReservationName.text = item.name
                    binding.tvReservationDescription.text = item.description

                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                    val startDate = item.dateStart?.toDate()
                    val endDate = item.dateEnd?.toDate()

                    val startText = startDate?.let { sdf.format(it) } ?: "Fecha no disponible"
                    val endText = endDate?.let { sdf.format(it) } ?: "Fecha no disponible"

                    binding.tvReservationDates.text = "Inicio: $startText\nFin: $endText"

                    binding.btnEditDates.setOnClickListener {
                        openDatePicker(item)
                    }
                }
            }
        )

        binding.rvReservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservations.adapter = adapter
    }

    private fun fetchReservations() {
        loggedInUser?.let { userDni ->
            firestore.collection("User")
                .document(userDni)
                .collection("Reservations")
                .get()
                .addOnSuccessListener { snapshot ->
                    reservationsList.clear()
                    for (document in snapshot) {
                        val reservation = document.toObject(Reservation::class.java)
                        reservationsList.add(reservation)
                    }
                    binding.rvReservations.adapter?.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error al cargar las reservas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openDatePicker(reservation: Reservation) {
        val calendar = Calendar.getInstance()

        // Configurar el DatePickerDialog
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        val newDateStart = calendar.timeInMillis
                        val newDateEnd = newDateStart + 3600000 // Sumar 1 hora

                        // Validar y actualizar la reserva
                        if (validateDate(newDateStart, newDateEnd, reservation.idPlace)) {
                            updateReservation(reservation, newDateStart, newDateEnd)
                        } else {
                            Toast.makeText(requireContext(), "La fecha seleccionada no está disponible.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateDate(start: Long, end: Long, idPlace: String): Boolean {
        for (reservation in reservationsList) {
            if (reservation.idPlace == idPlace &&
                ((start in reservation.getDateStartMillis()..reservation.getDateEndMillis()) ||
                        (end in reservation.getDateStartMillis()..reservation.getDateEndMillis()))) {
                return false
            }
        }
        return true
    }

    private fun updateReservation(reservation: Reservation, newDateStart: Long, newDateEnd: Long) {
        firestore.collection("User")
            .document(loggedInUser!!)
            .collection("Reservations")
            .document(reservation.idPlace)
            .update(mapOf(
                "dateStart" to Timestamp(Date(newDateStart)),
                "dateEnd" to Timestamp(Date(newDateEnd))
            ))
            .addOnSuccessListener {
                fetchReservations() // Recargar las reservas
                Toast.makeText(requireContext(), "Reserva actualizada con éxito.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al actualizar la reserva.", Toast.LENGTH_SHORT).show()
            }
    }
}