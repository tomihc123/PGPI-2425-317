package com.example.villactiva

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
class MainActivityTest {

    private lateinit var mainActivity: MainActivity

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mainActivity = Robolectric.buildActivity(MainActivity::class.java).create().get()

        // Inicializar FirebaseAuth mock
        `when`(mockFirebaseAuth.currentUser).thenReturn(null)

        // Simulamos SharedPreferences en MainActivity
        `when`(mainActivity.getSharedPreferences("VillactivaPrefs", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
    }

    @Test
    fun testOnCreate_WhenUserIsNotLoggedIn() {
        // Simula un SharedPreferences sin usuario logueado
        `when`(mockSharedPreferences.getString("logged_in_user", "")).thenReturn("")

        // Cargamos la actividad
        mainActivity.onCreate(null)

        // Verificar que si no hay usuario logueado, redirige al LoginActivity
        val intent = mainActivity.intent
        assert(intent.component?.className == "com.example.villactiva.LoginActivity")

        // Verificar que se finaliza la actividad
        verify(mainActivity).finish()
    }

    @Test
    fun testOnCreate_WhenUserIsLoggedIn() {
        // Simula que el usuario está logueado
        `when`(mockSharedPreferences.getString("logged_in_user", "")).thenReturn("userDNI")

        // Llamamos a onCreate()
        mainActivity.onCreate(null)

        // Verificar que el usuario está logueado y no redirige a LoginActivity
        val intent = mainActivity.intent
        assert(intent.component?.className != "com.example.villactiva.LoginActivity")

        // Verificar que el fragmento correcto es cargado
        val fragmentTransaction = verify(mainActivity.supportFragmentManager.beginTransaction(), times(1))
        verify(fragmentTransaction).replace(eq(R.id.container), any(HomeFragment::class.java))
    }

    @Test
    fun testSetupBottomNavigation_NavHomeSelected() {
        // Simula un usuario logueado
        `when`(mockSharedPreferences.getString("logged_in_user", "")).thenReturn("userDNI")

        // Cargamos la actividad
        mainActivity.onCreate(null)

        // Establece el listener para cuando se selecciona un ítem
        mainActivity.binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Verificar que HomeFragment es cargado
                    val transaction = mainActivity.supportFragmentManager.beginTransaction()
                    assert(transaction.isEmpty)
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }

        // Simula que el ítem de 'nav_home' es seleccionado
        mainActivity.binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    @Test
    fun testSetupBottomNavigation_NavLogout() {
        // Simula un usuario logueado
        `when`(mockSharedPreferences.getString("logged_in_user", "")).thenReturn("userDNI")

        // Cargamos la actividad
        mainActivity.onCreate(null)

        // Establece el listener para cuando se selecciona un ítem
        mainActivity.binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    // Verificar que se llama a cerrarSesion()
                    verify(mainActivity, times(1)).cerrarSesion()
                    return@setOnItemSelectedListener true
                }
                else -> false
            }
        }

        // Simula que el ítem de 'nav_logout' es seleccionado
        mainActivity.binding.bottomNavigation.selectedItemId = R.id.nav_logout
    }

    @Test
    fun testCerrarSesion() {
        // Simula que hay un usuario logueado
        `when`(mockSharedPreferences.getString("logged_in_user", "")).thenReturn("userDNI")

        // Llamamos al método cerrarSesion()
        mainActivity.cerrarSesion()

        // Verificar que se limpia SharedPreferences
        verify(mockSharedPreferences.edit()).clear()
        verify(mockSharedPreferences.edit()).apply()

        // Verificar que FirebaseAuth realiza el signOut
        verify(mockFirebaseAuth).signOut()

        // Verificar que redirige a LoginActivity
        val intent = mainActivity.intent
        assert(intent.component?.className == "com.example.villactiva.LoginActivity")
    }
}

