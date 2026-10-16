package com.example.sistemadetutorasuas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sistemadetutorasuas.data.model.Carrera
import com.example.sistemadetutorasuas.data.model.RegisterRequest
import com.example.sistemadetutorasuas.data.network.RetrofitClient
import com.example.sistemadetutorasuas.data.repository.TokenManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etCorreo: EditText
    private lateinit var spnCarrera: Spinner
    private lateinit var etPassword: EditText
    private lateinit var etPasswordConfirm: EditText
    private lateinit var btnRegister: Button
    private lateinit var pbRegisterLoading: ProgressBar
    private lateinit var tvVolverLogin: TextView
    private lateinit var tokenManager: TokenManager

    private var listaCarreras = listOf<Carrera>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        tokenManager = TokenManager(this)

        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)
        etCorreo = findViewById(R.id.etCorreo)
        spnCarrera = findViewById(R.id.spnCarrera)
        etPassword = findViewById(R.id.etPassword)
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm)
        btnRegister = findViewById(R.id.btnRegister)
        pbRegisterLoading = findViewById(R.id.pbRegisterLoading)
        tvVolverLogin = findViewById(R.id.tvVolverLogin)

        cargarCarreras()

        btnRegister.setOnClickListener { realizarRegistro() }
        tvVolverLogin.setOnClickListener { finish() }
    }

    private fun cargarCarreras() {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@RegisterActivity)
                val response = apiService.getCarreras()

                if (response.isSuccessful && response.body() != null) {
                    listaCarreras = response.body()!!.filter { it.activa }

                    val nombresCarreras = listaCarreras.map { it.nombre }
                    val adapter = ArrayAdapter(this@RegisterActivity, android.R.layout.simple_spinner_dropdown_item, nombresCarreras)
                    spnCarrera.adapter = adapter
                } else {
                    Toast.makeText(this@RegisterActivity, "Error al cargar las carreras", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Sin conexión al servidor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun realizarRegistro() {
        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val passwordConfirm = etPasswordConfirm.text.toString().trim()

        val posicionSeleccionada = spnCarrera.selectedItemPosition

        if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != passwordConfirm) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        if (listaCarreras.isEmpty() || posicionSeleccionada < 0) {
            Toast.makeText(this, "Selecciona una carrera válida", Toast.LENGTH_SHORT).show()
            return
        }

        // --- EL CAMBIO ESTÁ AQUÍ ---
        // Obtenemos el "nombre" de la carrera en lugar de su "id"
        val nombreCarrera = listaCarreras[posicionSeleccionada].nombre

        mostrarCargando(true)

        lifecycleScope.launch {
            try {
                // Pasamos "nombreCarrera" (String) a nuestro modelo
                val request = RegisterRequest(nombre, apellido, correo, password, passwordConfirm, nombreCarrera)
                val apiService = RetrofitClient.getApiService(this@RegisterActivity)
                val response = apiService.register(request)

                mostrarCargando(false)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    if (!authResponse.token.isNullOrEmpty()) {
                        tokenManager.saveToken(authResponse.token)
                        Toast.makeText(this@RegisterActivity, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                        irAlDashboard()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "El correo ya existe o los datos son inválidos", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                mostrarCargando(false)
                Toast.makeText(this@RegisterActivity, "Error de red", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        pbRegisterLoading.visibility = if (cargando) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !cargando
    }

    private fun irAlDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}