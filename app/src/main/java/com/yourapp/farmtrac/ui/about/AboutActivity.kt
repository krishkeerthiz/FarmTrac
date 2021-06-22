package com.yourapp.farmtrac.ui.about

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yourapp.farmtrac.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.aboutToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}