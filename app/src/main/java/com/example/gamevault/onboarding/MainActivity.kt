package com.example.gamevault.onboarding

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.gamevault.R
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), FragmentCommunicator {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Barra de estado morada + íconos blancos (combina con el degradado)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
    }

    override fun manageLoader(show: Boolean) {
        binding.loader.visibility = if (show) View.VISIBLE else View.GONE
    }
}