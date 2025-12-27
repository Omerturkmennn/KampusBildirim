package com.example.kampusbildirim

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.kampusbildirim.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        //Zaten giriş yapmışsa direkt ana sayfaya at
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
        //Giriş yap
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {

                        //ADMİN GİRİŞİ
                        if (email == "admin@gmail.com") {
                            Toast.makeText(requireContext(), "Admin Girişi Yapıldı", Toast.LENGTH_SHORT).show()
                            val intent = android.content.Intent(requireActivity(), AdminActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish() //Geri dönülmesin
                        } else {

                            //NORMAL KULLANICI
                            Toast.makeText(requireContext(), "Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                        //Activity Intent yerine Fragment Navigation kullanılıyor
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)}
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Hata: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Bilgileri giriniz.", Toast.LENGTH_SHORT).show()
            }
        }
        //Kayıt ol linki
        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        //ŞİFREMİ UNUTTUM
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                // E-posta kutusu doluysa direkt oraya gönder
                sendPasswordReset(email)
            } else {
                // Boşsa kullanıcıya soran bir pencere aç
                showResetPasswordDialog()
            }
        }
    }
    //YARDIMCI FONKSİYON: Dialog kutusu göster
    private fun showResetPasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Şifre Sıfırlama")
        builder.setMessage("Lütfen hesabınıza kayıtlı e-posta adresini girin:")

        //İçine yazı yazılabilecek bir alan oluştur
        val input = android.widget.EditText(requireContext())
        input.hint = "E-posta adresi"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Gönder") { _, _ ->
            val email = input.text.toString()
            if (email.isNotEmpty()) {
                sendPasswordReset(email)
            } else {
                Toast.makeText(requireContext(), "E-posta boş olamaz!", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("İptal") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    //YARDIMCI FONKSİYON: Firebase  istek at
    private fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Sıfırlama bağlantısı gönderildi! Mail kutunuzu kontrol edin.", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}