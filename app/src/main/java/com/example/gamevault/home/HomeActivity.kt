package com.example.gamevault.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gamevault.core.FragmentCommunicator
import com.example.gamevault.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), FragmentCommunicator {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun manageLoader(show: Boolean) { }
}
