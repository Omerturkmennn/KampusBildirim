package com.example.kampusbildirim

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kampusbildirim.databinding.ActivityAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminActivity: AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val db = FirebaseFirestore.getInstance()
    private val reportList = ArrayList<Report>()
    private lateinit var adminAdapter: AdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //RecyclerView kurulum
        binding.rvAdminReports.layoutManager = LinearLayoutManager(this)
        adminAdapter = AdminAdapter(reportList, db)
        binding.rvAdminReports.adapter = adminAdapter

        //Verileri getir
        fetchReports("Tümü")

        // ACİL DURUM DUYURUSU BUTONU
        binding.btnPostAnnouncement.setOnClickListener {
            showAnnouncementDialog()
        }

        //TÜMÜ BUTONU
        binding.btnFilterAll.setOnClickListener {
            updateButtonColors(binding.btnFilterAll)
            fetchReports("Tümü")
        }

        //BEKLEYENLER (AÇIK) BUTONU
        binding.btnFilterWaiting.setOnClickListener {
            updateButtonColors(binding.btnFilterWaiting)
            fetchReports("Açık") // Veritabanında status="Açık" olanlar
        }

        //ÇÖZÜLENLER BUTONU
        binding.btnFilterSolved.setOnClickListener {
            updateButtonColors(binding.btnFilterSolved)
            fetchReports("Çözüldü") // Veritabanında status="Çözüldü" olanlar
        }

        //ÇIKIŞ YAP BUTONU
        binding.btnAdminLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() //GERİ TUŞUNA BASINCA ADMİN PANELİNE DÖNMESİN!!!
        }
    }

    //DUYURU PENCERESİ
    private fun showAnnouncementDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Acil Duyuru Yayınla")
        builder.setMessage("Tüm kullanıcıların ana sayfasında görünecek mesajı giriniz:")

        val input = android.widget.EditText(this)
        input.hint = "Örn: Kütüphane 17:00'da kapanacak."
        builder.setView(input)

        builder.setPositiveButton("YAYINLA") { _, _ ->
            val message = input.text.toString().trim()
            if (message.isNotEmpty()) {
                saveAnnouncementToFirebase(message)
            }
        }
        builder.setNegativeButton("İptal", null)
        builder.show()
    }

    //FIREBASE KAYIT
    private fun saveAnnouncementToFirebase(message: String) {
        val announcement = hashMapOf(
            "text" to message,
            "timestamp" to com.google.firebase.Timestamp.now(), // Zaman damgası
            "isActive" to true
        )


        // ID yi 'current' yaparak hep tek bir duyuru olmasını sağlıyorum
        db.collection("announcements").document("current")
            .set(announcement)
            .addOnSuccessListener {
                Toast.makeText(this, "Duyuru başarıyla yayınlandı!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchReports(filterStatus: String) {
        binding.progressBarAdmin.visibility = View.VISIBLE

        //Temel sorgu oluşturucu
        var query: Query = db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        // ğer filtre "Tümü" değilse, 'whereEqualTo' ekle
        if (filterStatus != "Tümü") {
            query = query.whereEqualTo("status", filterStatus)
        }

        query.addSnapshotListener { value, error ->
            binding.progressBarAdmin.visibility = View.GONE

            if (error != null) {
                // ÖNEMLİ: Eğer burada hata alırsan Logcat'e bak, index oluşturma linki verecek.
                Toast.makeText(this, "Hata: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
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
                adminAdapter.notifyDataSetChanged()

                //Liste boşsa bilgi ver
                if (reportList.isEmpty()) {
                    Toast.makeText(this, "Bu filtrede bildirim bulunamadı.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
    private fun updateButtonColors(activeButton: android.widget.Button) {
        //Gri
        val passiveColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#B0BEC5"))
        //Aktif renk (Koyu Mavi/Gri)
        val activeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#607D8B"))

        //Hepsini önce pasif yap
        binding.btnFilterAll.backgroundTintList = passiveColor
        binding.btnFilterWaiting.backgroundTintList = passiveColor
        binding.btnFilterSolved.backgroundTintList = passiveColor

        // Sadece tıklananı aktif yap
        activeButton.backgroundTintList = activeColor
    }
}

