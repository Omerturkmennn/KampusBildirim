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
import android.widget.ArrayAdapter
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
                binding.imgSelected.setImageBitmap(imageBitmap)
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

            binding.btnSubmit.isEnabled = true
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

        //Spinneri doldur
        val reportTypes = arrayOf("Arıza", "Şikayet", "İstek", "Öneri", "Acil Durum")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, reportTypes)
        binding.spinnerType.adapter = spinnerAdapter

        binding.btnSelectImage.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        //Kameraya tıklanınca izin ister
        binding.btnSubmit.setOnClickListener {
            // Başlık ve Açıklama kontrolü
            val title = binding.etReportTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (title.isEmpty()) {
                binding.tilReportTitle.error = "Başlık giriniz"
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                binding.tilDescription.error = "Açıklama giriniz"
                return@setOnClickListener
            }
            // Fotoğraf zorunlu olsun mu? Senin eski kodunda zorunluydu, aynen bıraktım.
            if (selectedBitmap == null) {
                Toast.makeText(requireContext(), "Lütfen fotoğraf çekin!", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Her şey tamamsa konumu alıp yüklemeye başla
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getLocationAndSubmit()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    //Konumu Alıp Fonksiyonları Tetikleyen Ara Fonksiyon
    private fun getLocationAndSubmit() {
        binding.btnSubmit.text = "YÜKLENİYOR..."
        binding.btnSubmit.isEnabled = false

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                // YENİ: Başlık ve Tür bilgilerini de alıyoruz
                val title = binding.etReportTitle.text.toString().trim()
                val description = binding.etDescription.text.toString().trim()
                val type = binding.spinnerType.selectedItem.toString() // YENİ: Seçilen Tür

                if (location != null) {
                    uploadImageAndSaveReport(selectedBitmap!!, title, description, type, location.latitude, location.longitude)
                } else {
                    Toast.makeText(requireContext(), "Konum alınamadı, 0.0 kullanılıyor.", Toast.LENGTH_SHORT).show()
                    uploadImageAndSaveReport(selectedBitmap!!, title, description, type, 0.0, 0.0)
                }
            }.addOnFailureListener {
                // Hata durumu
                val title = binding.etReportTitle.text.toString().trim()
                val description = binding.etDescription.text.toString().trim()
                val type = binding.spinnerType.selectedItem.toString()

                uploadImageAndSaveReport(selectedBitmap!!, title, description, type, 0.0, 0.0)
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "İzin hatası!", Toast.LENGTH_SHORT).show()
        }
    }

    // FOTOĞRAFI YÜKLEME FONKSİYONU
    // FOTOĞRAFI YÜKLEME FONKSİYONU (Güncellendi: latitude ve longitude eklendi)
    private fun uploadImageAndSaveReport(bitmap: Bitmap, title: String, description: String, type: String, latitude: Double, longitude: Double) {


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
                    saveToFirestore(title, description, type, downloadUrl, latitude, longitude)
                }
            }
            .addOnFailureListener { e ->
                // Hata olursa

                binding.btnSubmit.isEnabled = true
                Toast.makeText(requireContext(), "Resim yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    //VERİTABANINA KAYDETME FONKSİYONU
    private fun saveToFirestore(title:String,description: String,type: String, imageUrl: String,latitude: Double,longitude: Double) {
        val userEmail = auth.currentUser?.email ?: "Anonim"
        val userId = auth.currentUser?.uid ?: ""

        // Kaydedilecek veri paketi
        val reportMap = hashMapOf(
            "title" to title,
            "type" to type,
            "status" to "Açık",
            "description" to description,
            "imageUrl" to imageUrl,
            "userEmail" to userEmail,
            "userId" to userId,
            "timestamp" to com.google.firebase.Timestamp.now(), //Şu anki zaman
            "latitude" to latitude,
            "longitude" to longitude,
        )

        // reports adlı collectiona ekle
        db.collection("reports")
            .add(reportMap)
            .addOnSuccessListener {
                //BAŞARILI

                binding.btnSubmit.isEnabled = true
                binding.btnSubmit.text = "BİLDİRİMİ GÖNDER"
                Toast.makeText(requireContext(), "Bildirim başarıyla gönderildi!", Toast.LENGTH_LONG).show()

                //Alanları temizle
                binding.etReportTitle.setText("")
                binding.etDescription.setText("")
                binding.imgSelected.setImageResource(android.R.drawable.ic_menu_camera) // imageViewReport -> imgSelected
                selectedBitmap = null
            }
            //Gönderim başarısız olursa
            .addOnFailureListener { e ->

                binding.btnSubmit.text = "BİLDİRİMİ GÖNDER"
                binding.btnSubmit.isEnabled = true
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