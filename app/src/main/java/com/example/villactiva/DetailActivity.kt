package com.example.villactiva

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.villactiva.databinding.DetailActivityBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: DetailActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("PLACE_NAME") ?: ""
        val description = intent.getStringExtra("PLACE_DESCRIPTION") ?: ""
        val image = intent.getStringExtra("PLACE_IMAGE") ?: ""

        binding.tvDetailName.text = name
        binding.tvDetailDescription.text = description
        Glide.with(this)
            .load(image)
            .into(binding.ivDetailImage)
    }
}
