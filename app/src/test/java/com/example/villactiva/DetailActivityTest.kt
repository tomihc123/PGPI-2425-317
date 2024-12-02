
import android.content.Context
import com.example.villactiva.DetailActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import java.util.Calendar

@Config(manifest = Config.NONE)
class DetailActivityTest {

    private lateinit var activity: DetailActivity
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockContext: Context
    private lateinit var mockCollectionReference: CollectionReference

    @Before
    fun setUp() {
        // Inicializar mocks
        mockContext = mock(Context::class.java)
        mockFirestore = mock(FirebaseFirestore::class.java)
        mockCollectionReference = mock(CollectionReference::class.java)

        // Inicia Robolectric
        activity = Robolectric.buildActivity(DetailActivity::class.java).create().get()

        // Asignar Firestore mockeado
        `when`(mockFirestore.collection("Reservations")).thenReturn(mockCollectionReference)
        activity.firestore = mockFirestore
    }

    @Test
    fun testValidateDates_shouldShowToastIfWeekend() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY) // Configurar día sábado
        activity.selectedStartDate = calendar.timeInMillis
        activity.selectedEndDate = activity.selectedStartDate + 3600000 // 1 hora

        // Simular la validación de fechas
        activity.validateDates()

        // Verificar que el Toast fue mostrado
        val shownToast = ShadowToast.getLatestToast()
        assert(shownToast != null) // Asegurarse de que el Toast no sea null
        assert(ShadowToast.getTextOfLatestToast().contains("No se permiten reservas los fines de semana")) // Verificar el mensaje del Toast
    }

    @Test
    fun testSaveReservation_shouldSaveToFirestore() {
        // Mock de datos
        val loggedInUser = "testUser"
        activity.loggedInUser = loggedInUser
        activity.idPlace = "testPlaceId"
        activity.selectedStartDate = System.currentTimeMillis() + 10000 // futuro
        activity.selectedEndDate = activity.selectedStartDate + 3600000 // 1 hora

        // Mock para la colección "Reservations"
        val mockCollectionReference = mock(CollectionReference::class.java)
        val mockDocumentReference = mock(DocumentReference::class.java)

        // Mock de la llamada collection("Reservations") para devolver el CollectionReference
        `when`(mockFirestore.collection("Reservations")).thenReturn(mockCollectionReference)

        // Mock de la llamada document() para devolver un DocumentReference
        `when`(mockCollectionReference.document()).thenReturn(mockDocumentReference)

        // Ejecutamos la acción de guardar la reserva
        activity.saveReservation()

        // Verificar si la llamada de set() se realizó correctamente en el DocumentReference
        verify(mockDocumentReference, times(1)).set(any()) // Verifica que el set() se haya llamado una vez
    }
}
