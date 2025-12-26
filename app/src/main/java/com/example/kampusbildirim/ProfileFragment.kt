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

       //KULLANICI BİLGİLERİNİ GÖSTER
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.tvUserEmail.text = currentUser.email

            //Kullanıcının kendi raporlarını getir
            fetchMyReports(currentUser.uid)
        }

        //ÇIKIŞ YAP BUTONU
        binding.btnLogout.setOnClickListener {
            auth.signOut() //Firebase den çıkış yap

           //Çıkış yapınca logine atma
             findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        //LİSTE AYARLARI
        binding.rvMyReports.layoutManager = LinearLayoutManager(requireContext())

        reportAdapter = ReportAdapter(reportList) { clickedReport ->
            //Detay sayfasına git
            val bundle = Bundle()
            bundle.putString("gonderilenBaslik", clickedReport.title)
            bundle.putString("gonderilenTur", clickedReport.type)
            bundle.putString("gonderilenResim", clickedReport.imageUrl)
            bundle.putString("gonderilenAciklama", clickedReport.description)
            bundle.putDouble("gonderilenLat", clickedReport.latitude ?: 0.0)
            bundle.putDouble("gonderilenLng", clickedReport.longitude ?: 0.0)

            findNavController().navigate(R.id.reportDetailFragment, bundle)
        }
        binding.rvMyReports.adapter = reportAdapter
    }

    private fun fetchMyReports(userId: String) {
        // SADECE BENİM RAPORLARIMI GETİR
        db.collection("reports")
            .whereEqualTo("userId", userId)

            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(requireContext(), "Hata: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (value != null) {
                    reportList.clear()
                    for (document in value.documents) {
                        val report = document.toObject(Report::class.java)
                        if (report != null) {
                            reportList.add(report)
                        }
                    }

                    //Listeyi tarihe göre sırala - Yeniden eskiye
                    // Firestoreda index hatası almamak için sıralamayı burada yapıyoruz
                    reportList.sortByDescending { it.timestamp?.seconds }

                    reportAdapter.notifyDataSetChanged()

                    // Liste boşsa uyarı göster
                    if (reportList.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                    }
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}