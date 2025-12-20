package com.example.kampusbildirim

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kampusbildirim.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Başlatma
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Kayıt Ol Butonuna Tıklanırsa
        binding.btnRegister.setOnClickListener {
            // Bilgileri al
            val name = binding.etName.text.toString().trim()
            val unit = binding.etUnit.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isNotEmpty() && unit.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {

                //  Firebase Auth ile Kullanıcı Oluştur
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->

                        // Kullanıcı ID al
                        val userId = authResult.user?.uid //neden nullabe zorunlu bilmiyorum kalsın boyle

                        //  Ekstra bilgileri Harita  yapısına koy
                        val userInfo = hashMapOf(
                            "name" to name,
                            "unit" to unit,
                            "email" to email,
                            "role" to "user" // Varsayılan rol= User
                        )

                        // bilgileri Firestore kaydet
                        if (userId != null) {
                            db.collection("Users").document(userId)
                                .set(userInfo)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                                    finish() // Sayfayı kapat, girişe dön
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Veritabanı hatası: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Kayıt Hatası: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Tüm alanları doldurunuz.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}