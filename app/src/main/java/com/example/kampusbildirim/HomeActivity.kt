package com.example.kampusbildirim

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.kampusbildirim.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        //  Controller al
        navController = navHostFragment.navController


        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_graph)
        navController.graph = graph

        //  menü tıklamalarını yönet manuel
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val destId = when (item.itemId) {
                R.id.homeFragment -> R.id.homeFragment
                R.id.mapFragment -> R.id.mapFragment
                R.id.addReportFragment -> R.id.addReportFragment
                R.id.profileFragment -> R.id.profileFragment
                else -> -1
            }

            if (destId != -1) {
                // zaten o sayfadaysak tekrar yükleme yapma
                if (navController.currentDestination?.id != destId) {
                    navController.navigate(destId)
                }
                true
            } else {
                false
            }
        }

        //geri tuşu senkronizasyonu
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.homeFragment ||
                destination.id == R.id.mapFragment ||
                destination.id == R.id.addReportFragment ||
                destination.id == R.id.profileFragment) {

                binding.bottomNavigationView.menu.findItem(destination.id)?.isChecked = true
            }
        }
    }
}