package com.example.kampusbildirim

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kampusbildirim.databinding.ItemReportBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReportAdapter(
    private val reportList: ArrayList<Report>,
    private val onItemClick: (Report) -> Unit // Tıklama fonksiyonu
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    class ReportViewHolder(val binding: ItemReportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return reportList.size
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val currentReport = reportList[position]

        //Açıklamayı yaz
        holder.binding.tvAciklama.text = currentReport.description

        //Tarihi formatla
        if (currentReport.timestamp != null) {
            val date = currentReport.timestamp.toDate()
            val format = SimpleDateFormat("dd MMMM HH:mm", Locale("tr", "TR"))
            holder.binding.tvTarih.text = format.format(date)
        }

        //Resmi GLİDE ile yükle
        Glide.with(holder.itemView.context)
            .load(currentReport.imageUrl)
            .centerCrop()
            .into(holder.binding.imgRaporGorseli)

        //Kullanıcı listedeki kutuya tıkladığında bu çalışacak
        holder.itemView.setOnClickListener {
            onItemClick(currentReport) // Tıklanan raporu HomeFragmenta postala
        }
    }
}