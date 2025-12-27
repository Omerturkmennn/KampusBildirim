package com.example.kampusbildirim

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kampusbildirim.databinding.ItemAdminReportBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminAdapter(
    private val reportList: ArrayList<Report>,
    private val db: FirebaseFirestore
) : RecyclerView.Adapter<AdminAdapter.AdminViewHolder>() {

    class AdminViewHolder(val binding: ItemAdminReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminViewHolder {
        val binding = ItemAdminReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reportList.size
    }

    override fun onBindViewHolder(holder: AdminViewHolder, position: Int) {
        val currentReport = reportList[position]

        //Verileri ekrana bas
        holder.binding.tvAdminTitle.text = currentReport.title
        holder.binding.tvAdminType.text = currentReport.type
        holder.binding.tvAdminDesc.text = currentReport.description
        holder.binding.tvAdminUser.text = "Gönderen: ${currentReport.userEmail}"

        //Durum göstergesi
        holder.binding.tvAdminStatus.text = currentReport.status

        //Duruma göre renk değişimi
        if (currentReport.status == "Çözüldü") {
            holder.binding.tvAdminStatus.setTextColor(Color.parseColor("#388E3C")) // Yeşil
            holder.binding.btnAdminApprove.visibility = View.GONE //Zaten çözülmüşse butonu gizle
        } else {
            holder.binding.tvAdminStatus.setTextColor(Color.parseColor("#D32F2F")) //Kırmızı
            holder.binding.btnAdminApprove.visibility = View.VISIBLE
        }

        //Resmi Yükle
        Glide.with(holder.itemView.context)
            .load(currentReport.imageUrl)
            .centerCrop()
            .into(holder.binding.imgAdminReport)

        //SİLME BUTONU
        holder.binding.btnAdminDelete.setOnClickListener {
            //Uyarı Penceresi (AlertDialog) Oluştur
            val builder = androidx.appcompat.app.AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Bildirimi Sil")
            builder.setMessage("Bu bildirimi kalıcı olarak silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")

            //"EVET" butonuna basılırsa silme işlemini yap
            builder.setPositiveButton("EVET") { dialog, which ->
                deleteReport(currentReport, position, holder.itemView.context)
            }

            //"HAYIR" butonuna basılırsa pencereyi kapat, hiçbir şey yapma
            builder.setNegativeButton("HAYIR") { dialog, which ->
                dialog.dismiss()
            }

            //Pencereyi göster
            builder.show()
        }

        //DETAY SAYFASINA GİT (
        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(holder.itemView.context, AdminDetailActivity::class.java)
            intent.putExtra("title", currentReport.title)
            intent.putExtra("description", currentReport.description)
            intent.putExtra("type", currentReport.type)
            intent.putExtra("imageUrl", currentReport.imageUrl)
            // Latitude ve Longitude verilerinin null gelme ihtimaline karşı kontrol
            intent.putExtra("lat", currentReport.latitude ?: 0.0)
            intent.putExtra("lng", currentReport.longitude ?: 0.0)

            holder.itemView.context.startActivity(intent)
        }

        //ÇÖZÜLDÜ (ONAYLA) BUTONU
        holder.binding.btnAdminApprove.setOnClickListener {
            updateStatus(currentReport, position, holder.itemView.context)
        }
    }

    private fun deleteReport(report: Report, position: Int, context: android.content.Context) {
        //userId ve timestamp kullanarak dökümanı bulmaya çalışıyoruz (ID olmadığı için)
        db.collection("reports")
            .whereEqualTo("userId", report.userId)
            .whereEqualTo("timestamp", report.timestamp)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete().addOnSuccessListener {
                        Toast.makeText(context, "Bildirim silindi.", Toast.LENGTH_SHORT).show()

                        //Listeden de silip ekranı güncelle
                        if (position < reportList.size) {
                            reportList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, reportList.size)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Silinemedi: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStatus(report: Report, position: Int, context: android.content.Context) {
        db.collection("reports")
            .whereEqualTo("userId", report.userId)
            .whereEqualTo("timestamp", report.timestamp)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("status", "Çözüldü")
                        .addOnSuccessListener {
                            Toast.makeText(context, "Durum güncellendi!", Toast.LENGTH_SHORT).show()
                            // Manuel olarak listedeki veriyi de güncelle ki ekran yenilensin
                            // (Normalde snapshot listener activityde dinlediği için otomatik olur ama garanti olsun diye böyle dursun)
                        }
                }
            }
    }
}