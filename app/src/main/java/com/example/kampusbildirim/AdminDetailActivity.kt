package com.example.kampusbildirim

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kampusbildirim.databinding.ActivityAdminDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class AdminDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAdminDetailBinding
    private lateinit var mMap: GoogleMap
    private var lat = 0.0
    private var lng = 0.0
    private var title = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Intent ile gelen verileri al
        title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description")
        val type = intent.getStringExtra("type")
        val imageUrl = intent.getStringExtra("imageUrl")
        lat = intent.getDoubleExtra("lat", 0.0)
        lng = intent.getDoubleExtra("lng", 0.0)

        //Yazıları yerleştir
        binding.tvAdminDetailTitle.text = title
        binding.tvAdminDetailType.text = type
        binding.tvAdminDetailDesc.text = description

        //Resmi yükle
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(imageUrl).into(binding.ivAdminDetailImage)
        }

        //Haritayı hazırla
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapAdminDetail) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (lat != 0.0 && lng != 0.0) {
            val location = LatLng(lat, lng)
            mMap.addMarker(MarkerOptions().position(location).title(title))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }
}
