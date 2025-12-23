package com.example.kampusbildirim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        reportAdapter = ReportAdapter(reportList)
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