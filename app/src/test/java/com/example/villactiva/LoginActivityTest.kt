package com.example.villactiva

import android.content.Context
import android.content.SharedPreferences
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@Config(manifest = Config.NONE)
class LoginActivityTest {

    private lateinit var loginActivity: LoginActivity

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        loginActivity = Robolectric.buildActivity(LoginActivity::class.java).create().get()
        loginActivity.firestore = mockFirestore

        // Simulando SharedPreferences
        val sharedPreferences = mock(SharedPreferences::class.java)
        `when`(loginActivity.getSharedPreferences("VillactivaPrefs", Context.MODE_PRIVATE)).thenReturn(sharedPreferences)
    }

    @Test
    fun testLoginActivity_SharedPreferencesUserLoggedIn() {
        // Simulamos que ya hay un usuario logueado en SharedPreferences
        val mockSharedPrefs = mock(SharedPreferences::class.java)
        val editor = mock(SharedPreferences.Editor::class.java)
        `when`(mockSharedPrefs.getString("logged_in_user", null)).thenReturn("userDNI")
        `when`(mockSharedPrefs.edit()).thenReturn(editor)

        loginActivity.getSharedPreferences("VillactivaPrefs", Context.MODE_PRIVATE).edit()
        loginActivity.onCreate(null)

        // Verificar que el usuario es redirigido a MainActivity
        val intent = loginActivity.intent
        assert(intent.component?.className == "com.example.villactiva.MainActivity")
    }

    @Test
    fun testLoginActivity_EmptyDniField() {
        // Dejar el campo de DNI vacío y hacer clic en el botón de login
        val mockDni = ""
        val dniField = loginActivity.findViewById<EditText>(R.id.etDni)
        dniField.setText(mockDni)

        loginActivity.findViewById<Button>(R.id.btnLogin).performClick()

        // Verificar que se muestra un Toast para indicar que el DNI está vacío
        val toastMessage = ShadowToast.getTextOfLatestToast()
        assert(toastMessage == "Por favor, introduce tu DNI")
    }

    @Test
    fun testLoginActivity_UserExistsInFirebase() {
        // Crear un mock de DocumentSnapshot
        val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)

        // Simulamos que el documento existe
        `when`(mockDocumentSnapshot.exists()).thenReturn(true)

        // Crear un mock de Task<DocumentSnapshot>
        val mockTask = mock(Task::class.java) as Task<DocumentSnapshot>

        // Simular que el Task es exitoso y devuelve el DocumentSnapshot
        `when`(mockTask.isSuccessful).thenReturn(true)
        `when`(mockTask.result).thenReturn(mockDocumentSnapshot)

        // Simulamos la llamada a Firestore
        `when`(mockFirestore.collection("User").document("userDNI").get()).thenReturn(mockTask)

        // Llamamos a la función de verificación de usuario
        loginActivity.verificarUsuario("userDNI")

        // Verificar que el usuario se guardó en SharedPreferences
        val sharedPreferences = loginActivity.getSharedPreferences("VillactivaPrefs", Context.MODE_PRIVATE)
        verify(sharedPreferences.edit(), times(1)).putString("logged_in_user", "userDNI")

        // Verificar que se navegó a MainActivity
        val intent = loginActivity.intent
        assert(intent.component?.className == "com.example.villactiva.MainActivity")
    }

    @Test
    fun testLoginActivity_UserDoesNotExistInFirebase() {
        // Crear un mock de DocumentSnapshot
        val mockDocumentSnapshot = mock(DocumentSnapshot::class.java)

        // Simulamos que el documento no existe
        `when`(mockDocumentSnapshot.exists()).thenReturn(false)

        // Crear un mock de Task<DocumentSnapshot>
        val mockTask = mock(Task::class.java) as Task<DocumentSnapshot>

        // Simular que el Task es exitoso y devuelve el DocumentSnapshot
        `when`(mockTask.isSuccessful).thenReturn(true)
        `when`(mockTask.result).thenReturn(mockDocumentSnapshot)

        // Simulamos la llamada a Firestore
        `when`(mockFirestore.collection("User").document("userDNI").get()).thenReturn(mockTask)

        // Llamamos a la función de verificación de usuario
        loginActivity.verificarUsuario("userDNI")

        // Verificar que se mostró el mensaje de error
        val toast = ShadowToast.getTextOfLatestToast()
        assert(toast.contains("El usuario no existe"))
    }

    @Test
    fun testLoginActivity_FirebaseFailure() {
        // Simulamos un fallo al consultar Firestore
        val exception = Exception("Firestore error")
        `when`(mockFirestore.collection("User").document("userDNI").get()).thenThrow(exception)

        loginActivity.verificarUsuario("userDNI")

        // Verificar que se muestra un mensaje de error
        val toastMessage = ShadowToast.getTextOfLatestToast()
        assert(toastMessage.contains("Error al verificar usuario"))
    }

    @Test
    fun testLoginActivity_OnBackPressed() {
        // Verificar que onBackPressed() no hace nada
        loginActivity.onBackPressed()
        // No se espera ningún cambio de comportamiento
    }
}

