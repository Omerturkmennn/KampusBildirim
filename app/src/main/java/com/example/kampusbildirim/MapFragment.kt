package com.example.kampusbildirim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.kampusbildirim.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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

        //İĞNEYE TIKLAMA OLAYI
        mMap.setOnMarkerClickListener { marker ->
            //İğnenin içine sakladığımız rapor objesini al
            val report = marker.tag as? Report

            if (report != null) {
                // Detay sayfasına gitmek için paketi (bundle) hazırla
                val bundle = Bundle()
                bundle.putString("gonderilenId", report.reportId)
                bundle.putString("gonderilenBaslik", report.title)
                bundle.putString("gonderilenTur", report.type)
                bundle.putString("gonderilenResim", report.imageUrl)
                bundle.putString("gonderilenAciklama", report.description)
                bundle.putDouble("gonderilenLat", report.latitude ?: 0.0)
                bundle.putDouble("gonderilenLng", report.longitude ?: 0.0)

                //Navigasyon ile detay sayfasına yolla
                findNavController().navigate(R.id.reportDetailFragment, bundle)
            }
            false //false dönersek varsayılan davranış (kamera odaklanması) da çalışır
        }

        //Veritabanındaki raporları çek ve haritaya ekle
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
                    val report = document.toObject(Report::class.java) //Veriyi nesneye çevir(markera eklemekiçin lazım)
                    val lat = document.getDouble("latitude") ?: 0.0
                    val lng = document.getDouble("longitude") ?: 0.0
                    val description = document.getString("description") ?: "Rapor"

                    //Eğer geçerli bir konum varsa (0.0 değilse)
                    if (lat != 0.0 && lng != 0.0) {
                        val location = LatLng(lat, lng)

                        //RENK AYARLAMA
                        //Gelen türe göre iğne rengini seçer
                        val markerColor = when (report.type) {
                            "Arıza" -> BitmapDescriptorFactory.HUE_RED      // Kırmızı
                            "Şikayet" -> BitmapDescriptorFactory.HUE_ORANGE // Turuncu
                            "İstek" -> BitmapDescriptorFactory.HUE_BLUE     // Mavi
                            "Öneri" -> BitmapDescriptorFactory.HUE_GREEN    // Yeşil
                            "Acil Durum" -> BitmapDescriptorFactory.HUE_VIOLET // Mor
                            else -> BitmapDescriptorFactory.HUE_AZURE       // Varsayılan (Açık Mavi)
                        }

                        // İĞNEYİ OLUŞTURMA
                        val marker = mMap.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title(report.title) //Başlık
                                .snippet(report.description) //Altına küçük açıklama
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)) //Rengi Uygula
                        )

                        marker?.tag = report

                        // Kamerayı son eklenen rapora odakla(zoom seviyesi: 15)
                        // Not:Çok fazla iğne varsa sadece ilkine odaklanmak daha iyi olabilir ama şimdilik böyle kalsın
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