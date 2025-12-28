package com.example.kampusbildirim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kampusbildirim.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    //HomeFragment daa kullanılan Adapter ın aynısını kullanılıyor
    private val reportList = ArrayList<Report>()
    private lateinit var reportAdapter: ReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Adapter Kurulumu (ID yi de gönder)
        binding.rvMyReports.layoutManager = LinearLayoutManager(requireContext())
        reportAdapter = ReportAdapter(reportList) { clickedReport ->
            //Detay sayfasına git
            val bundle = Bundle()
            bundle.putString("gonderilenId", clickedReport.reportId)
            bundle.putString("gonderilenBaslik", clickedReport.title)
            bundle.putString("gonderilenTur", clickedReport.type)
            bundle.putString("gonderilenResim", clickedReport.imageUrl)
            bundle.putString("gonderilenAciklama", clickedReport.description)
            bundle.putDouble("gonderilenLat", clickedReport.latitude ?: 0.0)
            bundle.putDouble("gonderilenLng", clickedReport.longitude ?: 0.0)

            findNavController().navigate(R.id.reportDetailFragment, bundle)
        }
        binding.rvMyReports.adapter = reportAdapter

        // KULLANICI BİLGİLERİ
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.tvUserEmail.text = currentUser.email
            // Varsayılan olarak Raporlarımı getir
            fetchMyReports(currentUser.uid)
        }

        // ÇIKIŞ YAP
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        // --- TAB BUTONLARI ---
        binding.btnTabMyReports.setOnClickListener {
            updateTabColors(isMyReports = true)
            if (currentUser != null) fetchMyReports(currentUser.uid)
        }

        binding.btnTabFollowing.setOnClickListener {
            updateTabColors(isMyReports = false)
            if (currentUser != null) fetchFollowedReports(currentUser.uid)
        }
    }

    // RENK GÜNCELLEME (Aktif butonu Mavi, Pasifi Gri yapar)
    private fun updateTabColors(isMyReports: Boolean) {
        val activeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1565C0"))
        val passiveColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#B0BEC5"))
        val white = android.graphics.Color.WHITE
        val black = android.graphics.Color.BLACK

        if (isMyReports) {
            binding.btnTabMyReports.backgroundTintList = activeColor
            binding.btnTabMyReports.setTextColor(white)
            binding.btnTabFollowing.backgroundTintList = passiveColor
            binding.btnTabFollowing.setTextColor(black)
        } else {
            binding.btnTabMyReports.backgroundTintList = passiveColor
            binding.btnTabMyReports.setTextColor(black)
            binding.btnTabFollowing.backgroundTintList = activeColor
            binding.btnTabFollowing.setTextColor(white)
        }
    }

    private fun fetchMyReports(userId: String) {
        db.collection("reports")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { value, error ->
                if (_binding == null) return@addSnapshotListener
                if (error != null) return@addSnapshotListener

                if (value != null) {
                    reportList.clear()
                    for (document in value.documents) {
                        val report = document.toObject(Report::class.java)
                        if (report != null) reportList.add(report)
                    }
                    reportList.sortByDescending { it.timestamp?.seconds }
                    reportAdapter.notifyDataSetChanged()

                    binding.tvEmptyState.text = "Henüz bir rapor göndermediniz."
                    binding.tvEmptyState.visibility = if (reportList.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    private fun fetchFollowedReports(userId: String) {
        // 1. Önce kullanıcının takip listesini (following array) çek
        db.collection("Users").document(userId).get()
            .addOnSuccessListener { document ->
                if (_binding == null) return@addOnSuccessListener

                val followingIds = document.get("following") as? List<String>

                // Eğer liste boşsa veya yoksa direkt boş ekran göster
                if (followingIds.isNullOrEmpty()) {
                    reportList.clear()
                    reportAdapter.notifyDataSetChanged()
                    binding.tvEmptyState.text = "Henüz kimseyi takip etmiyorsunuz."
                    binding.tvEmptyState.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                // 2. Takip edilen ID'leri kullanarak Raporları çek
                // NOT: Firestore 'whereIn' sorgusu en fazla 10 eleman kabul eder.
                // Okul projesi olduğu için 10'dan fazla takip etmeyeceğini varsayıyoruz.
                // Eğer 10'dan fazla olursa sadece ilk 10 tanesini alırız: followingIds.take(10)

                db.collection("reports")
                    .whereIn(FieldPath.documentId(), followingIds.take(10))
                    .addSnapshotListener { value, error ->
                        if (_binding == null) return@addSnapshotListener

                        if (value != null) {
                            reportList.clear()
                            for (doc in value.documents) {
                                val report = doc.toObject(Report::class.java)
                                if (report != null) reportList.add(report)
                            }
                            reportAdapter.notifyDataSetChanged()

                            binding.tvEmptyState.text = "Takip edilen bildirim bulunamadı."
                            binding.tvEmptyState.visibility = if (reportList.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}