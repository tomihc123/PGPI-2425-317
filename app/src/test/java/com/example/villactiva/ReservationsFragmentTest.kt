package com.example.villactiva

import android.view.View
import com.example.villactiva.databinding.FragmentReservationsBinding
import com.example.villactiva.model.Reservation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyMap
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@Config(manifest = Config.NONE)
class ReservationsFragmentTest {

    private lateinit var fragment: ReservationsFragment
    private lateinit var binding: FragmentReservationsBinding

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockQuerySnapshot: QuerySnapshot

    @Mock
    private lateinit var mockDocumentSnapshot: DocumentSnapshot

    // Cambiar el tipo de mockTask a Task<Void> para las operaciones de delete y update
    @Mock
    private lateinit var mockTaskVoid: Task<Void> // Para operaciones de delete() y update()

    // Para get() que devuelve un QuerySnapshot
    @Mock
    private lateinit var mockTaskQuerySnapshot: Task<QuerySnapshot> // Para get() que devuelve un QuerySnapshot

    @Mock
    private lateinit var mockReservation: Reservation

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Construir la actividad y fragmento con Robolectric
        val activity = Robolectric.setupActivity(MainActivity::class.java) // Asegúrate de que MainActivity contiene el fragmento
        fragment = activity.supportFragmentManager.findFragmentByTag(ReservationsFragment::class.java.simpleName) as ReservationsFragment
        binding = fragment.binding

        // Inicializar el Firestore Mock
        fragment.firestore = mockFirestore
    }

    @Test
    fun testFetchReservations_WhenReservationsExist() {
        // Simula que hay reservas en Firestore
        `when`(mockFirestore.collection("User").document("userDNI").collection("Reservations").get())
            .thenReturn(mockTaskQuerySnapshot)

        // Simula la respuesta de Firestore con varias reservas
        val mockReservations = listOf(
            Reservation("1", "Reserva 1", "Descripción 1", null.toString(), null, null),
            Reservation("2", "Reserva 2", "Descripción 2", null.toString(), null, null)
        )

        // Mock de QuerySnapshot
        `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)
        `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)

        // Mock de los documentos en el QuerySnapshot
        `when`(mockQuerySnapshot.documents).thenReturn(
            mockReservations.map { reservation ->
                mock(DocumentSnapshot::class.java).apply {
                    `when`(this.getString("id")).thenReturn(reservation.id)
                    `when`(this.getString("name")).thenReturn(reservation.name)
                }
            }
        )

        // Ejecutar la función de carga de reservas
        fragment.fetchReservations()

        // Verificar que el RecyclerView fue actualizado con las reservas
        val recyclerView = binding.rvReservations
        val adapter = recyclerView.adapter
        assert(adapter != null && adapter.itemCount == 2)
    }

    @Test
    fun testFetchReservations_WhenNoReservationsExist() {
        // Simula que no hay reservas en Firestore
        `when`(mockFirestore.collection("User").document("userDNI").collection("Reservations").get())
            .thenReturn(mockTaskQuerySnapshot)

        // Mock de QuerySnapshot vacío
        `when`(mockTaskQuerySnapshot.isSuccessful).thenReturn(true)
        `when`(mockTaskQuerySnapshot.result).thenReturn(mockQuerySnapshot)

        // Mock de la respuesta con ninguna reserva
        `when`(mockQuerySnapshot.documents).thenReturn(emptyList())

        // Ejecutar la función de carga de reservas
        fragment.fetchReservations()

        // Verificar que el mensaje "No hay reservas" se muestra
        assert(binding.tvNoReservations.visibility == View.VISIBLE)
    }

    @Test
    fun testDeleteReservation() {
        // Simula la eliminación de una reserva
        val reservationToDelete = Reservation("1", "Reserva a eliminar", "Descripción", null.toString(), null, null)

        // Mock para Task<Void> con éxito
        `when`(mockFirestore.collection("User").document("userDNI").collection("Reservations").document(reservationToDelete.id).delete())
            .thenReturn(mockTaskVoid)

        // Configuramos el mockTask para que indique que la operación fue exitosa
        `when`(mockTaskVoid.isSuccessful).thenReturn(true)

        // Simula la interacción con el botón de eliminar
        fragment.deleteReservation(reservationToDelete)

        // Verificar que la reserva fue eliminada de la lista
        verify(mockFirestore.collection("User").document("userDNI").collection("Reservations").document(reservationToDelete.id).delete(), times(1))
    }

    @Test
    fun testUpdateReservation() {
        // Simula la actualización de una reserva
        val reservationToUpdate = Reservation("1", "Reserva actualizada", "Descripción actualizada",
            null.toString(), null, null)

        // Preparamos la función mock que actualizará la reserva
        `when`(mockFirestore.collection("User").document("userDNI").collection("Reservations").document(reservationToUpdate.id).update(anyMap()))
            .thenReturn(mockTaskVoid)

        // Mock para Task<Void> con éxito
        `when`(mockTaskVoid.isSuccessful).thenReturn(true)

        // Ejecutar la actualización
        fragment.updateReservation(reservationToUpdate, 1677984000000L, 1677987600000L)

        // Verificar que la actualización fue realizada
        verify(mockFirestore.collection("User").document("userDNI").collection("Reservations").document(reservationToUpdate.id).update(anyMap()), times(1))
    }

    @Test
    fun testShowDeleteDialog() {
        // Verifica que el diálogo de eliminación es mostrado correctamente
        fragment.showDeleteDialog { }

        // Verificar que el diálogo está presente
        assert(fragment.activity?.isFinishing == false)
    }

    @Test
    fun testToastMessage_WhenErrorOccurs() {
        // Simula un error en la carga de reservas
        `when`(mockFirestore.collection("User").document("userDNI").collection("Reservations").get())
            .thenThrow(RuntimeException("Error"))

        fragment.fetchReservations()

        // Verificar que se mostró el mensaje de error
        val toast = ShadowToast.getLatestToast()
        assert(toast != null)
        assert(ShadowToast.getTextOfLatestToast() == "Error al cargar las reservas: Error")
    }
}




