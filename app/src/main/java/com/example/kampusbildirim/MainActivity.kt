package com.example.kampusbildirim

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kampusbildirim.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()


        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // Giriş Yap Butonuna Tıklanırsa
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()


            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Firebase ile giriş yapmayı dene
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        // Giriş Başarılı mesaj
                        Toast.makeText(this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show()

                        //Ana Sayfaya Yönlendirme
                        val intent= Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish() //geri tusuna basınca tekrar login ekranına dönmemesini sağlar
                    }
                    .addOnFailureListener { exception ->
                        // Hata bildirimi
                        Toast.makeText(this, "Hata: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Lütfen e-posta ve şifreyi giriniz.", Toast.LENGTH_SHORT).show()
            }
        }

        // Kayıt Ol tıklanırsa
        binding.tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}