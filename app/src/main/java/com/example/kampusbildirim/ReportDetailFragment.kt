package com.example.kampusbildirim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.kampusbildirim.databinding.FragmentReportDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class ReportDetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap

    //Gelen verileri tutmak için değişkenler
    private var gelenLat = 0.0
    private var gelenLng = 0.0
    private var gelenAciklama = ""
    private var gelenBaslik=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //PAKETİ AÇ (Argumentstan verileri al)
        arguments?.let { bundle ->
            val gelenResimUrl = bundle.getString("gonderilenResim")
            gelenBaslik = bundle.getString("gonderilenBaslik") ?: "Başlık Yok"
            val gelenTur = bundle.getString("gonderilenTur") ?: "Genel"
            gelenAciklama = bundle.getString("gonderilenAciklama") ?: "Açıklama yok"
            gelenLat = bundle.getDouble("gonderilenLat")
            gelenLng = bundle.getDouble("gonderilenLng")

            //VERİLERİ EKRANA YERLEŞTİR
            binding.tvDetailTitle.text = gelenBaslik
            binding.tvDetailType.text = gelenTur
            binding.tvDetailDescription.text = gelenAciklama

            //Resmi Glide ile yükle
            if (!gelenResimUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(gelenResimUrl)
                    .centerCrop()
                    .into(binding.ivDetailImage)
            }
        }

        //HARİTAYI BAŞLAT
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapDetail) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    // Harita hazır olunca burası çalışır
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Eğer geçerli bir konum geldiyse haritada göster
        if (gelenLat != 0.0 && gelenLng != 0.0) {
            val location = LatLng(gelenLat, gelenLng)

            // ,Kırmızı iğneyi dik
            mMap.addMarker(MarkerOptions().position(location).title(gelenAciklama))

            //Kamerayı oraya odakla (Zoom seviyesi: 15)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}