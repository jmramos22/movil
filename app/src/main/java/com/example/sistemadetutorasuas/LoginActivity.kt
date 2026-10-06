package com.example.sistemadetutorasuas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sistemadetutorasuas.data.model.LoginRequest
import com.example.sistemadetutorasuas.data.network.RetrofitClient
import com.example.sistemadetutorasuas.data.repository.TokenManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tokenManager = TokenManager(this)

        // Si ya cuenta con un token guardado, saltar directo al Dashboard
        if (!tokenManager.getToken().isNullOrEmpty()) {
            irAlDashboard()
            return
        }

        etCorreo = findViewById(R.id.etCorreo)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin() {
        val correo = etCorreo.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarCargando(true)

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@LoginActivity)
                val response = apiService.login(LoginRequest(correo, password))

                mostrarCargando(false)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    val token = authResponse.token

                    if (!token.isNullOrEmpty()) {
                        tokenManager.saveToken(token)
                        Toast.makeText(this@LoginActivity, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                        irAlDashboard()
                    } else {
                        Toast.makeText(this@LoginActivity, "Respuesta sin token", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Errores HTTP 401 / 422
                    Toast.makeText(this@LoginActivity, "Credenciales incorrectas o invalidas", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                mostrarCargando(false)
                Toast.makeText(this@LoginActivity, "Error de conexion con la API: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !cargando
    }

    private fun irAlDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish() // Finaliza esta actividad para no volver al login con el botón 'Atrás'
    }
}