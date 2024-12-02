package com.example.villactiva

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.villactiva.adapter.GenericSimpleRecyclerBindingInterface
import com.example.villactiva.adapter.SimpleGenericRecyclerAdapter
import com.example.villactiva.databinding.ItemPlaceBinding
import com.example.villactiva.model.Place
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@Config(manifest = Config.NONE)
class HomeFragmentTest {

    private lateinit var fragment: HomeFragment
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockActivity: FragmentActivity

    @Before
    fun setUp() {
        mockActivity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        fragment = HomeFragment()
        mockActivity.supportFragmentManager.beginTransaction().add(fragment, null).commit()

        mockFirestore = mock(FirebaseFirestore::class.java)
        fragment.firestore = mockFirestore
    }

    @Test
    fun testRecyclerViewPopulation() {
        // Crear un mock de Place
        val mockPlace = Place("Test Place", "Test Description", "https://example.com/image.jpg")
        val mockDocumentSnapshot = mock(QueryDocumentSnapshot::class.java)
        `when`(mockDocumentSnapshot.toObject(Place::class.java)).thenReturn(mockPlace)

        // Crear una lista mutable de QueryDocumentSnapshot
        val mockDocumentList = mutableListOf(mockDocumentSnapshot)

        // Simular que QuerySnapshot tiene un iterador de tipo MutableIterator<QueryDocumentSnapshot>
        val mockQuerySnapshot = mock(QuerySnapshot::class.java)
        `when`(mockQuerySnapshot.iterator()).thenReturn(mockDocumentList.iterator())

        // Simulamos que Firestore's collection get() devuelva un Task<QuerySnapshot>
        val mockTask = mock(Task::class.java) as Task<QuerySnapshot>
        `when`(mockTask.isSuccessful).thenReturn(true)
        `when`(mockTask.result).thenReturn(mockQuerySnapshot)
        `when`(mockFirestore.collection("Places").get()).thenReturn(mockTask)

        // Llamamos al método fetchPlaces() que hará la consulta a Firestore
        fragment.fetchPlaces()

        // Verificar que el RecyclerView fue actualizado
        val recyclerView = fragment.view?.findViewById<RecyclerView>(R.id.rvPlaces)
        val adapter = recyclerView?.adapter as SimpleGenericRecyclerAdapter<*, *>
        verify(adapter).notifyDataSetChanged()
    }

    @Test
    fun testToastOnFetchFailure() {
        // Simulate Firestore failure
        val exception = RuntimeException("Firestore error")
        `when`(mockFirestore.collection("Places").get()).thenThrow(exception)

        // Call fetchPlaces
        fragment.fetchPlaces()

        // Verify that a Toast was shown
        val toast = ShadowToast.getTextOfLatestToast()
        assert(toast.contains("Error al cargar los lugares"))
    }

    @Test
    fun testItemClickNavigatesToDetailActivity() {
        // Simula un lugar
        val mockPlace = Place("Test Place", "Test Description", "https://example.com/image.jpg")
        val itemBinding = mock(ItemPlaceBinding::class.java)
        val mockIntent = mock(Intent::class.java)

        // Simula el clic en un lugar
        `when`(itemBinding.root.context).thenReturn(mockActivity)
        `when`(mockActivity.startActivity(mockIntent)).thenReturn(Unit)

        // Crea el adapter
        val adapter = SimpleGenericRecyclerAdapter(
            dataSet = listOf(mockPlace),
            bindingInflater = ItemPlaceBinding::inflate,
            bindingInterface = object : GenericSimpleRecyclerBindingInterface<Place, ItemPlaceBinding> {
                override fun bindData(
                    item: Place,
                    binding: ItemPlaceBinding,
                    position: Int,
                    adapter: SimpleGenericRecyclerAdapter<Place, ItemPlaceBinding>
                ) {
                    binding.root.setOnClickListener {
                        val intent = Intent(mockActivity, DetailActivity::class.java)
                        intent.putExtra("PLACE_NAME", item.name)
                        intent.putExtra("PLACE_DESCRIPTION", item.description)
                        intent.putExtra("PLACE_IMAGE", item.image)
                        mockActivity.startActivity(intent)
                    }
                }
            }
        )

        // Llama a la función que simula el clic
        adapter.onBindViewHolder(adapter.createViewHolder(itemBinding.root, 0), 0)

        // Verifica que la intención fue llamada
        verify(mockActivity).startActivity(mockIntent)
    }
}


