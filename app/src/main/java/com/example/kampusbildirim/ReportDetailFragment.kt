package com.example.kampusbildirim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.kampusbildirim.databinding.FragmentReportDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class ReportDetailFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var mMap: GoogleMap

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    //Veriler
    private var reportId = ""
    private var lat = 0.0
    private var lng = 0.0
    private var tur = ""
    private var baslik = ""
    private var aciklama = ""

    //Takip Durumu
    private var isFollowing = false

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
            reportId = bundle.getString("gonderilenId") ?: ""
            baslik = bundle.getString("gonderilenBaslik") ?: ""
            tur = bundle.getString("gonderilenTur") ?: ""
            aciklama = bundle.getString("gonderilenAciklama") ?: ""
            val resimUrl = bundle.getString("gonderilenResim")
            lat = bundle.getDouble("gonderilenLat")
            lng = bundle.getDouble("gonderilenLng")

            //VERİLERİ EKRANA YERLEŞTİR
            binding.tvDetailTitle.text = baslik
            binding.tvDetailType.text = tur
            binding.tvDetailDescription.text = aciklama

            if (!resimUrl.isNullOrEmpty()) {
                Glide.with(this).load(resimUrl).centerCrop().into(binding.ivDetailImage)
            }
        }

        //Başlangıçta takip durumunu kontrol et
        checkIfFollowing()

        //Butona Tıklama Olayı
        binding.btnFollow.setOnClickListener {
            toggleFollowState()
        }

        //HARİTAYI BAŞLAT
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapDetail) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun checkIfFollowing() {
        val userId = auth.currentUser?.uid ?: return

        // Kullanıcının "following" listesinde bu rapor var mı?
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val followingList = document.get("following") as? List<String>
                    if (followingList != null && followingList.contains(reportId)) {
                        isFollowing = true
                        updateFollowButtonUI() // Dolu Yıldız Yap
                    }
                }
            }
    }

    private fun toggleFollowState() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Giriş yapmalısınız.", Toast.LENGTH_SHORT).show()
            return
        }

        if (isFollowing) {
            // Takipten Çık (Listeden Sil)
            db.collection("Users").document(userId)
                .update("following", FieldValue.arrayRemove(reportId))
                .addOnSuccessListener {
                    isFollowing = false
                    updateFollowButtonUI()
                    Toast.makeText(requireContext(), "Takipten çıkıldı.", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Takip Et (Listeye Ekle)
            db.collection("Users").document(userId)
                .update("following", FieldValue.arrayUnion(reportId))
                .addOnSuccessListener {
                    isFollowing = true
                    updateFollowButtonUI()
                    Toast.makeText(requireContext(), "Takip ediliyor!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Eğer 'following' alanı hiç yoksa hata verebilir, o yüzden set ile merge yapmayı deneyebiliriz ama update genelde array oluşturur.
                    // Garanti olsun diye Users dökümanı yoksa oluşturulmalı (zaten Register'da oluşturduk).
                    Toast.makeText(requireContext(), "İşlem başarısız: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateFollowButtonUI() {
        if (isFollowing) {
            // TAKİP EDİLİYORSA: Dolu yıldız ve SARI renk
            binding.btnFollow.setImageResource(android.R.drawable.btn_star_big_on)
            binding.btnFollow.setColorFilter(android.graphics.Color.parseColor("#FBC02D"))
        } else {
            // TAKİP EDİLMİYORSA: Boş yıldız ve GRİ renk
            binding.btnFollow.setImageResource(android.R.drawable.btn_star_big_off)
            binding.btnFollow.setColorFilter(android.graphics.Color.parseColor("#757575"))
        }
    }

    // Harita hazır olunca burası çalışır
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Eğer geçerli bir konum geldiyse haritada göster
        if (lat != 0.0 && lng != 0.0) {
            val location = LatLng(lat, lng)

            // --- RENK AYARLAMA  ---
            val hue = when (tur) {
                "Arıza" -> BitmapDescriptorFactory.HUE_RED      // Kırmızı
                "Şikayet" -> BitmapDescriptorFactory.HUE_ORANGE // Turuncu
                "İstek" -> BitmapDescriptorFactory.HUE_BLUE     // Mavi
                "Öneri" -> BitmapDescriptorFactory.HUE_GREEN    // Yeşil
                "Acil Durum" -> BitmapDescriptorFactory.HUE_VIOLET // Mor
                else -> BitmapDescriptorFactory.HUE_AZURE       // Varsayılan
            }

            mMap.addMarker(MarkerOptions().position(location).title(baslik).icon(BitmapDescriptorFactory.defaultMarker(hue)))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}