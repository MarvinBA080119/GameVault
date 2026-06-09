package com.example.gamevault.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gamevault.R
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), FragmentCommunicator {

    lateinit var binding: ActivityHomeBinding
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_dark)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        // Oculta la barra inferior en pantallas de detalle (full-screen)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility =
                when (destination.id) {
                    R.id.gameDetailFragment -> View.GONE
                    else -> View.VISIBLE
                }
        }
    }

    override fun manageLoader(show: Boolean) {
        if (show) {
            binding.loader.visibility = View.VISIBLE
            binding.loader.playAnimation()
        } else {
            binding.loader.cancelAnimation()
            binding.loader.visibility = View.GONE
        }
    }
}
