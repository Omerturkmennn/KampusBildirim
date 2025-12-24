package com.example.kampusbildirim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.kampusbildirim.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore


class MapFragment : Fragment() , OnMapReadyCallback {
    //Harita ve veritabanı
    private lateinit var mMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // XML deki harita fragmentini bul ve başlat
        val mapFragment=childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    //Harita hazır olunca bura calısır
    override fun onMapReady(googleMap: GoogleMap) {
        mMap= googleMap

        // Veritabanındaki raporları çek ve haritaya ekle
        fetchReportsAndShowOnMap()
    }

    private fun fetchReportsAndShowOnMap() {
        db.collection("reports").get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "Henüz hiç rapor yok.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                //Tüm belgeleri döngüyle gez
                for (document in result) {
                    val lat = document.getDouble("latitude") ?: 0.0
                    val lng = document.getDouble("longitude") ?: 0.0
                    val description = document.getString("description") ?: "Rapor"

                    //Eğer geçerli bir konum varsa (0.0 değilse)
                    if (lat != 0.0 && lng != 0.0) {
                        val location = LatLng(lat, lng)

                        // Haritaya İğne (Marker) Ekle
                        mMap.addMarker(MarkerOptions().position(location).title(description))

                        // Kamerayı son eklenen rapora odakla (Zoom seviyesi: 15)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Veriler alınamadı: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}