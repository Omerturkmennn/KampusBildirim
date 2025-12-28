package com.example.kampusbildirim

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kampusbildirim.databinding.FragmentHomeBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale


class HomeFragment : Fragment() {

    private val fullList = ArrayList<Report>()    // Veritabanından gelen TÜM veriler (Yedek)
    private val displayList = ArrayList<Report>() // Ekranda o an GÖSTERİLEN veriler (Filtreli)

    private val db= FirebaseFirestore.getInstance()

    private lateinit var reportAdapter: ReportAdapter

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // Hangi kategorinin seçili olduğunu tutan değişken
    private var currentCategory = "Tümü" //default Tümü

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
        reportAdapter = ReportAdapter(displayList) { clickedReport ->

            //Tıklanan raporun bilgilerini Bundle içine koyuyoruz
            val bundle = Bundle()
            bundle.putString("gonderilenId", clickedReport.reportId)
            bundle.putString("gonderilenBaslik", clickedReport.title)
            bundle.putString("gonderilenTur", clickedReport.type)
            bundle.putString("gonderilenResim", clickedReport.imageUrl)
            bundle.putString("gonderilenAciklama", clickedReport.description)
            bundle.putDouble("gonderilenLat", clickedReport.latitude ?: 0.0)
            bundle.putDouble("gonderilenLng", clickedReport.longitude ?: 0.0)

            //Çantayı alıp Detay Sayfasına gidiyoruz
            //(nav_graph.xmlde oluşturulanm action IDsini kullanıyoruz)
            findNavController().navigate(R.id.action_homeFragment_to_reportDetailFragment, bundle)
        }
        binding.recyclerViewReports.adapter = reportAdapter
        fetchReports() //Verileri getir
        listenForAnnouncements()//acil duyurular

        // ARAMA ÇUBUĞU AYARLARI
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Her harf yazıldığında listeyi filtrele
                filterList(newText, currentCategory)
                return true
            }
        })

        //KATEGORİ BUTONLARI AYARLARI
        setupFilterButtons()
    }

    //Butonlara tıklama özelliği ekleyen yardımcı fonksiyon
    private fun setupFilterButtons() {
        val buttons = listOf(
            binding.btnFilterAll,
            binding.btnFilterAriza,
            binding.btnFilterSikayet,
            binding.btnFilterIstek,
            binding.btnFilterOneri
        )

        val clickListener = View.OnClickListener { view ->
            val clickedButton = view as Button
            val category = clickedButton.text.toString()

            //Görsel Ayar:Tıklanan butonu koyu yap, diğerlerini açık gri yap
            buttons.forEach {
                it.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#B0BEC5")) //Pasif Gri
            }
            clickedButton.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#455A64")) //Aktif Koyu

            //Listeyi Filtrele
            currentCategory = category
            //Arama çubuğunda yazı varsa onu da dikkate al
            val currentSearchText = binding.searchView.query.toString()
            filterList(currentSearchText, currentCategory)
        }

        //Tüm butonlara bu özelliği ata
        buttons.forEach { it.setOnClickListener(clickListener) }
    }

    //ANA FİLTRELEME MANTIĞI
    private fun filterList(searchText: String?, category: String) {
        displayList.clear() // Ekran listesini temizle

        val search = searchText?.lowercase(Locale.getDefault()) ?: "" //Küçük harfe çevir

        //Yedek listedeki (fullList) her elemanı kontrol et
        for (item in fullList) {

            // Kural 1:Kategori uyuyor mu veya "Tümü" mü seçili?
            val categoryMatch = if (category == "Tümü") true else item.type == category

            // Kural 2: Arama metni Başlıkta veya açıklamada geçiyo mu
            val searchMatch = if (search.isEmpty()) true else {
                item.title.lowercase(Locale.getDefault()).contains(search) ||
                        item.description.lowercase(Locale.getDefault()).contains(search)
            }

            //İki kural da tutuyorsa ekrana ekle
            if (categoryMatch && searchMatch) {
                displayList.add(item)
            }
        }
        // Adapter'a haber ver: "Liste değişti, ekranı yenile"
        reportAdapter.notifyDataSetChanged()
    }

    //Duyuru dinleme fonskiyonu
    private fun listenForAnnouncements() {
        db.collection("announcements").document("current")
            .addSnapshotListener { snapshot, error ->
                if (_binding == null) return@addSnapshotListener

                if (error != null || snapshot == null || !snapshot.exists()) {
                    // Hata varsa veya veri yoksa kartı gizle
                    binding.cardAnnouncement.visibility = View.GONE
                    return@addSnapshotListener
                }

                val text = snapshot.getString("text")
                // isActive kontrolü (Varsayılan true kabul edelim)
                val isActive = snapshot.getBoolean("isActive") ?: true

                if (isActive && !text.isNullOrEmpty()) {
                    //Mesaj varsa KARTI GÖSTER ve metni yaz
                    binding.tvAnnouncementText.text = text
                    binding.cardAnnouncement.visibility = View.VISIBLE
                } else {
                    // Mesaj yoksa gizle
                    binding.cardAnnouncement.visibility = View.GONE
                }
            }
    }



    private fun fetchReports() {

        // REPORTS koleksiyonunu tarihe göre sırala (yeniden eskiye)
        db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->

                if (_binding == null) return@addSnapshotListener

                if (error != null) {
                    Toast.makeText(requireContext(), "Hata: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                if (value != null) {
                    fullList.clear() //Yedek listeyi temizle
                    for (document in value.documents) {
                        val report = document.toObject(Report::class.java)
                        if (report != null) {
                            fullList.add(report) //Veriyi yedek listeye at
                        }
                    }
                    //Veriler geldi, şimdi filtre fonksiyonunu çağırarak ekrana yansımasını sağla
                    filterList(binding.searchView.query.toString(), currentCategory)
                }
            }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}