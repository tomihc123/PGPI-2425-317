package com.example.villactiva

import android.content.Intent
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
import com.example.villactiva.databinding.FragmentHomeBinding
import com.example.villactiva.databinding.ItemPlaceBinding
import com.example.villactiva.model.Place
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val placesList = mutableListOf<Place>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupRecyclerView()
        fetchPlaces()
        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = SimpleGenericRecyclerAdapter(
            dataSet = placesList,
            bindingInflater = ItemPlaceBinding::inflate,
            bindingInterface = object :
                GenericSimpleRecyclerBindingInterface<Place, ItemPlaceBinding> {
                override fun bindData(
                    item: Place,
                    binding: ItemPlaceBinding,
                    position: Int,
                    adapter: SimpleGenericRecyclerAdapter<Place, ItemPlaceBinding>
                ) {
                    binding.tvPlaceName.text = item.name
                    binding.tvPlaceDescription.text = item.description
                    Glide.with(this@HomeFragment)
                        .load(item.image) // URL de la imagen
                        .centerCrop()
                        .into(binding.ivPlaceImage)

                    binding.root.setOnClickListener {
                        // Navegar a la pantalla de detalle del lugar
                        val intent = Intent(requireContext(), DetailActivity::class.java)
                        intent.putExtra("PLACE_NAME", item.name)
                        intent.putExtra("PLACE_DESCRIPTION", item.description)
                        intent.putExtra("PLACE_IMAGE", item.image)
                        startActivity(intent)
                    }
                }
            }
        )
        binding.rvPlaces.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaces.adapter = adapter
    }

    private fun fetchPlaces() {
        firestore.collection("Places").get()
            .addOnSuccessListener { snapshot ->
                placesList.clear()
                for (document in snapshot) {
                    val place = document.toObject(Place::class.java)
                    placesList.add(place)
                }
                binding.rvPlaces.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Error al cargar los lugares: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
