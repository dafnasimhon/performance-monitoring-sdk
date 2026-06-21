package com.example.mysdk

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mysdk.api.RetrofitClient
import com.example.mysdk.api.models.LoginRequest
import com.example.mysdk.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.title = "Checkout / Login"
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Login with correct credentials → 200
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            performLogin(username, password)
        }

        // Intentional wrong password → 401 for error demo
        binding.btnWrongLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            performLogin(username, "wrongpassword")
        }
    }

    private fun performLogin(username: String, password: String) {
        binding.btnLogin.isEnabled      = false
        binding.btnWrongLogin.isEnabled = false
        binding.tvStatus.visibility     = View.GONE

        lifecycleScope.launch {
            val result = tracked("POST", "/auth/login") {
                RetrofitClient.api.login(LoginRequest(username, password))
            }

            if (result != null) {
                binding.tvStatus.text      = "✅ Login successful! Token received."
                binding.tvStatus.setTextColor(Color.parseColor("#16A34A"))
            } else {
                binding.tvStatus.text      = "❌ Login failed (401 — wrong password)"
                binding.tvStatus.setTextColor(Color.parseColor("#DC2626"))
            }
            binding.tvStatus.visibility     = View.VISIBLE
            binding.btnLogin.isEnabled      = true
            binding.btnWrongLogin.isEnabled = true
        }
    }
}
