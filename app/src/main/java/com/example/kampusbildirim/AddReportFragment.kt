package com.example.kampusbildirim

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.kampusbildirim.databinding.FragmentAddReportBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID


class AddReportFragment : Fragment() {

    private val storage= FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var _binding: FragmentAddReportBinding? = null
    private val binding get() = _binding!!
    private var selectedBitmap: Bitmap? = null //Çekilen foto burda tutulacak

    //Kamera açılır, fotoğraf çekilir, küçük boyutlu bitmap alınır ve imageView da gösterilir
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap //fotoğraf alınır
            if (imageBitmap != null) {
                selectedBitmap = imageBitmap
                binding.imageViewReport.setImageBitmap(imageBitmap)
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Kamera izni vermeden fotoğraf çekemezsiniz.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCapturePhoto.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        //Kameraya tıklanınca izin ister
        binding.btnSubmitReport.setOnClickListener {
            val description = binding.etDescription.text.toString()

            if (selectedBitmap == null) {
                Toast.makeText(requireContext(), "Lütfen fotoğraf çekin!", Toast.LENGTH_SHORT).show()
            } else if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen açıklama yazın.", Toast.LENGTH_SHORT).show()
            } else {
                // Her sey hazır yüklemeyi baslat
                uploadImageAndSaveReport(selectedBitmap!!, description)
            }
        }

    }

    // FOTOĞRAFI YÜKLEME FONKSİYONU
    private fun uploadImageAndSaveReport(bitmap: Bitmap, description: String) {
        //Yükleniyor çubuğunu göster, butonu kilitle
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitReport.isEnabled = false

        //Resim ismini rastgele oluştur,format abcd52163.jpg şeklinde olcak
        val fileName = "images/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        //Resmi sıkıştır ve byte dizisine çevir
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        //STORAGEA YÜKLE
        storageRef.putBytes(data)
            .addOnSuccessListener {
                //Yükleme başarılı
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    //Linki aldık
                    saveToFirestore(description, downloadUrl)
                }
            }
            .addOnFailureListener { e ->
                // Hata olursa
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitReport.isEnabled = true
                Toast.makeText(requireContext(), "Resim yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //VERİTABANINA KAYDETME FONKSİYONU
    private fun saveToFirestore(description: String, imageUrl: String) {
        val userEmail = auth.currentUser?.email ?: "Anonim"
        val userId = auth.currentUser?.uid ?: ""

        // Kaydedilecek veri paketi
        val reportMap = hashMapOf(
            "description" to description,
            "imageUrl" to imageUrl,
            "userEmail" to userEmail,
            "userId" to userId,
            "timestamp" to com.google.firebase.Timestamp.now() // Şu anki zaman
        )

        // reports adlı collectiona ekle
        db.collection("reports")
            .add(reportMap)
            .addOnSuccessListener {
                //BAŞARILI
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitReport.isEnabled = true
                Toast.makeText(requireContext(), "Bildirim başarıyla gönderildi!", Toast.LENGTH_LONG).show()

                // Alanları temizle
                binding.etDescription.setText("")
                binding.imageViewReport.setImageResource(android.R.drawable.ic_menu_camera)
                selectedBitmap = null
            }
            //Gönderim başarısız olursa
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitReport.isEnabled = true
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            cameraLauncher.launch(intent)
        } else {
            Toast.makeText(requireContext(), "Kamera bulunamadı!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

}