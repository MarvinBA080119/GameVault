package com.example.gamevault.onboarding

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), FragmentCommunicator {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun manageLoader(show: Boolean) {
        binding.loader.visibility = if (show) View.VISIBLE else View.GONE
    }
}
