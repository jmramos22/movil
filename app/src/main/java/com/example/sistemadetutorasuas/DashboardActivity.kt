package com.example.sistemadetutorasuas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sistemadetutorasuas.data.network.RetrofitClient
import com.example.sistemadetutorasuas.data.repository.TokenManager
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvUsuario: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvCarrera: TextView
    private lateinit var tvRol: TextView
    private lateinit var btnLogout: Button
    private lateinit var pbLoading: ProgressBar
    private lateinit var llUserData: LinearLayout
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tokenManager = TokenManager(this)

        tvUsuario = findViewById(R.id.tvUsuario)
        tvCorreo = findViewById(R.id.tvCorreo)
        tvCarrera = findViewById(R.id.tvCarrera)
        tvRol = findViewById(R.id.tvRol)
        btnLogout = findViewById(R.id.btnLogout)
        pbLoading = findViewById(R.id.pbLoading)
        llUserData = findViewById(R.id.llUserData)

        obtenerDatosUsuario()

        btnLogout.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun obtenerDatosUsuario() {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@DashboardActivity)
                val response = apiService.getUser()

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    tvUsuario.text = "Usuario: ${user.nombre} ${user.apellido}"
                    tvCorreo.text = "Correo: ${user.correo_institucional}"
                    tvCarrera.text = "Carrera: ${user.carrera}"
                    tvRol.text = "Rol: ${user.rol}"

                    pbLoading.visibility = View.GONE
                    llUserData.visibility = View.VISIBLE
                } else {
                    // Token expirado o inválido -> Redirigir al Login
                    tokenManager.clearToken()
                    irAlLogin()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "Error al cargar datos: ${e.message}", Toast.LENGTH_LONG).show()
                tokenManager.clearToken()
                irAlLogin()
            }
        }
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@DashboardActivity)
                apiService.logout()
            } catch (e: Exception) {
                // Si falla la red, borramos el token de todas formas en local
            } finally {
                tokenManager.clearToken()
                irAlLogin()
            }
        }
    }

    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}