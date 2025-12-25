package com.example.kampusbildirim

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.kampusbildirim.databinding.FragmentAddReportBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    //Konum Servisi
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

    //Konum İzni İsteyici
    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // İzin verildiyse konumu al ve işlemi devam ettir
            getLocationAndSubmit()
        } else {
            Toast.makeText(requireContext(), "Konum izni olmadan rapor gönderilemez.", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.GONE
            binding.btnSubmitReport.isEnabled = true
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

       //Konum servisini başlat
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getLocationAndSubmit()
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }

    }

    //Konumu Alıp Fonksiyonları Tetikleyen Ara Fonksiyon
    private fun getLocationAndSubmit() {
        // Yükleniyor...
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitReport.isEnabled = false

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                val description = binding.etDescription.text.toString()
                if (location != null) {
                    //Konum var, koordinatları yolla
                    uploadImageAndSaveReport(selectedBitmap!!, description, location.latitude, location.longitude)
                } else {
                    //Konum alınamadı (GPS kapalı olabilir), 0.0, 0.0 olarak yolla
                    Toast.makeText(requireContext(), "Konum alınamadı, konumsuz gönderiliyor.", Toast.LENGTH_SHORT).show()
                    uploadImageAndSaveReport(selectedBitmap!!, description, 0.0, 0.0)
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Konum hatası: ${it.message}", Toast.LENGTH_SHORT).show()
                //Hata olsa da gönder
                uploadImageAndSaveReport(selectedBitmap!!, binding.etDescription.text.toString(), 0.0, 0.0)
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "İzin hatası!", Toast.LENGTH_SHORT).show()
        }
    }

    // FOTOĞRAFI YÜKLEME FONKSİYONU
    // FOTOĞRAFI YÜKLEME FONKSİYONU (Güncellendi: latitude ve longitude eklendi)
    private fun uploadImageAndSaveReport(bitmap: Bitmap, description: String, latitude: Double, longitude: Double) {


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
                    saveToFirestore(description, downloadUrl, latitude, longitude)
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
    private fun saveToFirestore(description: String, imageUrl: String,latitude: Double,longitude: Double) {
        val userEmail = auth.currentUser?.email ?: "Anonim"
        val userId = auth.currentUser?.uid ?: ""

        // Kaydedilecek veri paketi
        val reportMap = hashMapOf(
            "description" to description,
            "imageUrl" to imageUrl,
            "userEmail" to userEmail,
            "userId" to userId,
            "timestamp" to com.google.firebase.Timestamp.now(), // Şu anki zaman
            "latitude" to latitude,  
            "longitude" to longitude
        )

        // reports adlı collectiona ekle
        db.collection("reports")
            .add(reportMap)
            .addOnSuccessListener {
                //BAŞARILI
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitReport.isEnabled = true
                Toast.makeText(requireContext(), "Bildirim başarıyla gönderildi!", Toast.LENGTH_LONG).show()

                //Alanları temizle
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