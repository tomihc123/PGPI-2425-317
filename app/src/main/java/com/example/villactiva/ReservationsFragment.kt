package com.example.villactiva

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    lateinit var binding: FragmentReservationsBinding
    private val reservationsList = mutableListOf<Reservation>()
    var firestore = FirebaseFirestore.getInstance()
    private var loggedInUser: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loggedInUser = arguments?.getString("USER_DNI")
    }

    companion object {
        fun newInstance(userDni: String): ReservationsFragment {
            val fragment = ReservationsFragment()
            val args = Bundle()
            args.putString("USER_DNI", userDni)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReservationsBinding.inflate(inflater, container, false)
        setupToolbar()
        setupRecyclerView()
        fetchReservations()
        return binding.root
    }

    private fun setupToolbar() {
        loggedInUser?.let { userDni ->
            firestore.collection("User").document(userDni).get()
                .addOnSuccessListener { document ->
                    val userName = document.getString("name") ?: "Usuario"
                    binding.toolbarReservations.title = "Hola, $userName"
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al cargar el usuario.", Toast.LENGTH_SHORT).show()
                }
        }
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

                    // Acción para editar las fechas de la reserva
                    binding.btnEditDates.setOnClickListener {
                        openDatePicker(item)
                    }

                    // Acción para eliminar la reserva
                    binding.ivDeleteReservation.setOnClickListener {
                        showDeleteDialog {
                            deleteReservation(item)
                        }
                    }
                }
            }
        )

        binding.rvReservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservations.adapter = adapter
    }

    fun fetchReservations() {
        loggedInUser?.let { userDni ->
            firestore.collection("User")
                .document(userDni)
                .collection("Reservations")
                .get()
                .addOnSuccessListener { snapshot ->
                    reservationsList.clear()
                    for (document in snapshot) {
                        val reservation = document.toObject(Reservation::class.java)
                        reservation.id = document.id
                        reservationsList.add(reservation)
                    }

                    // Mostrar mensaje si no hay reservas
                    if (reservationsList.isEmpty()) {
                        binding.tvNoReservations.visibility = View.VISIBLE
                        binding.rvReservations.visibility = View.GONE
                    } else {
                        binding.tvNoReservations.visibility = View.GONE
                        binding.rvReservations.visibility = View.VISIBLE
                    }

                    binding.rvReservations.adapter?.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error al cargar las reservas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun showDeleteDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Estás seguro de que deseas eliminar esta reserva?")
            .setPositiveButton("Sí") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun deleteReservation(reservation: Reservation) {
        loggedInUser?.let { userDni ->
            firestore.collection("User")
                .document(userDni)
                .collection("Reservations")
                .document(reservation.id)
                .delete()
                .addOnSuccessListener {
                    reservationsList.remove(reservation)
                    binding.rvReservations.adapter?.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Reserva eliminada con éxito.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error al eliminar la reserva: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openDatePicker(reservation: Reservation) {
        val calendar = Calendar.getInstance()

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
                        val newDateEnd = newDateStart + 3600000 // Sumar 1 hora automáticamente

                        validateAndUpdateReservation(reservation, newDateStart, newDateEnd)
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
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    private fun validateAndUpdateReservation(reservation: Reservation, start: Long, end: Long) {
        firestore.collection("Reservations")
            .whereEqualTo("idPlace", reservation.idPlace)
            .get()
            .addOnSuccessListener { snapshot ->
                var isValid = true

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = start

                // Validar fines de semana
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    isValid = false
                    Toast.makeText(requireContext(), "No se permiten reservas los fines de semana.", Toast.LENGTH_SHORT).show()
                }

                // Validar horario permitido
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                if (hour < 9 || hour >= 21) {
                    isValid = false
                    Toast.makeText(requireContext(), "La hora debe estar entre las 9:00 y 21:00.", Toast.LENGTH_SHORT).show()
                }

                // Validar fechas en el pasado
                if (start < System.currentTimeMillis()) {
                    isValid = false
                    Toast.makeText(requireContext(), "No puedes seleccionar fechas en el pasado.", Toast.LENGTH_SHORT).show()
                }

                // Validar duración mínima de 1 hora
                if (end <= start) {
                    isValid = false
                    Toast.makeText(requireContext(), "La reserva debe durar al menos 1 hora.", Toast.LENGTH_SHORT).show()
                }

                // Validar conflictos con otras reservas
                for (document in snapshot) {
                    val existingStart = document.getTimestamp("dateStart")?.toDate()?.time ?: 0L
                    val existingEnd = document.getTimestamp("dateEnd")?.toDate()?.time ?: 0L

                    if (!(end <= existingStart || start >= existingEnd) && document.id != reservation.id) {
                        isValid = false
                        Toast.makeText(requireContext(), "Ya existe una reserva en esta franja horaria.", Toast.LENGTH_SHORT).show()
                        break
                    }
                }

                // Si todo es válido, actualizar la reserva
                if (isValid) {
                    updateReservation(reservation, start, end)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error al validar las fechas.", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateReservation(reservation: Reservation, newDateStart: Long, newDateEnd: Long) {
        loggedInUser?.let { userDni ->
            firestore.collection("User")
                .document(userDni)
                .collection("Reservations")
                .document(reservation.id)
                .update(
                    mapOf(
                        "dateStart" to Timestamp(Date(newDateStart)),
                        "dateEnd" to Timestamp(Date(newDateEnd))
                    )
                )
                .addOnSuccessListener {
                    fetchReservations()
                    Toast.makeText(requireContext(), "Reserva actualizada con éxito.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error al actualizar la reserva: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
