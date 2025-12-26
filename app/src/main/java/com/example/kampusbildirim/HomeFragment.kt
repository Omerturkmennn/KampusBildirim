package com.example.kampusbildirim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kampusbildirim.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class HomeFragment : Fragment() {

    private val db= FirebaseFirestore.getInstance()
    private val reportList= ArrayList<Report>()
    private lateinit var reportAdapter: ReportAdapter

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //RecyclerView ile oluşturulan listeyi kur
        binding.recyclerViewReports.layoutManager = LinearLayoutManager(requireContext())

        //Adapterı başlatırken artık ikinci parametre (Tıklama Olayı) veriyoruz
        reportAdapter = ReportAdapter(reportList) { clickedReport ->

            //Tıklanan raporun bilgilerini Bundle içine koyuyoruz
            val bundle = Bundle()
            bundle.putString("gonderilenBaslik", clickedReport.title)
            bundle.putString("gonderilenTur", clickedReport.type)
            bundle.putString("gonderilenResim", clickedReport.imageUrl)
            bundle.putString("gonderilenAciklama", clickedReport.description)
            bundle.putDouble("gonderilenLat", clickedReport.latitude ?: 0.0)
            bundle.putDouble("gonderilenLng", clickedReport.longitude ?: 0.0)

            //Çantayı alıp Detay Sayfasına gidiyoruz
            //(nav_graph.xmlde oluşturulanm action ID'sini kullanıyoruz)
            findNavController().navigate(R.id.action_homeFragment_to_reportDetailFragment, bundle)
        }
        binding.recyclerViewReports.adapter = reportAdapter
        fetchReports() //Verileri getir

    }

    private fun fetchReports(){

        // REPORTS koleksiyonunu tarihe göre sırala (yeniden eskiye)
        db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->

                if (error != null) {
                    Toast.makeText(requireContext(), "Hata: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (value != null) {
                    reportList.clear() //Eski listeyi temizle
                    for (document in value.documents) {
                        val report = document.toObject(Report::class.java)
                        if (report != null) {
                            reportList.add(report)
                        }
                    }
                    //Listeyi Yenile
                    reportAdapter.notifyDataSetChanged()
                }
            }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}