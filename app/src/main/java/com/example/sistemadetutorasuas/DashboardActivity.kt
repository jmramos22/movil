package com.example.sistemadetutorasuas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sistemadetutorasuas.data.model.PasswordRequest
import com.example.sistemadetutorasuas.data.network.RetrofitClient
import com.example.sistemadetutorasuas.data.repository.TokenManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvUsuario: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvCarrera: TextView
    private lateinit var tvRol: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var btnCambiarFoto: Button

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnActualizarPassword: Button

    private lateinit var btnLogout: Button
    private lateinit var pbLoading: ProgressBar
    private lateinit var llUserData: LinearLayout
    private lateinit var tokenManager: TokenManager

    // Lanzador para abrir la galería del teléfono
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            subirImagenPerfil(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tokenManager = TokenManager(this)

        tvUsuario = findViewById(R.id.tvUsuario)
        tvCorreo = findViewById(R.id.tvCorreo)
        tvCarrera = findViewById(R.id.tvCarrera)
        tvRol = findViewById(R.id.tvRol)
        ivAvatar = findViewById(R.id.ivAvatar)
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto)

        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnActualizarPassword = findViewById(R.id.btnActualizarPassword)

        btnLogout = findViewById(R.id.btnLogout)
        pbLoading = findViewById(R.id.pbLoading)
        llUserData = findViewById(R.id.llUserData)

        obtenerDatosUsuario()

        btnLogout.setOnClickListener { cerrarSesion() }

        btnCambiarFoto.setOnClickListener {
            // Abrir selector de imágenes
            pickImageLauncher.launch("image/*")
        }

        btnActualizarPassword.setOnClickListener {
            actualizarPassword()
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

                    // Cargar imagen con Glide si existe
                    if (!user.imagen_perfil.isNullOrEmpty()) {
                        Glide.with(this@DashboardActivity)
                            .load(user.imagen_perfil)
                            .circleCrop()
                            .into(ivAvatar)
                    }

                    pbLoading.visibility = View.GONE
                    llUserData.visibility = View.VISIBLE
                } else {
                    tokenManager.clearToken()
                    irAlLogin()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun subirImagenPerfil(uri: Uri) {
        pbLoading.visibility = View.VISIBLE
        llUserData.visibility = View.GONE

        lifecycleScope.launch {
            try {
                // 1. Convertir la URI del teléfono a un Archivo físico temporal
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = File.createTempFile("avatar_", ".jpg", cacheDir)
                tempFile.outputStream().use { output ->
                    inputStream?.copyTo(output)
                }

                // 2. Preparar el archivo para enviarlo a Laravel
                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                // NOTA: "imagen" es el nombre del campo que Laravel espera recibir.
                val body = MultipartBody.Part.createFormData("imagen", tempFile.name, requestFile)

                val apiService = RetrofitClient.getApiService(this@DashboardActivity)
                val response = apiService.uploadProfileImage(body)

                if (response.isSuccessful) {
                    Toast.makeText(this@DashboardActivity, "Foto actualizada", Toast.LENGTH_SHORT).show()
                    obtenerDatosUsuario() // Recargar datos para ver la foto nueva
                } else {
                    Toast.makeText(this@DashboardActivity, "Error al subir foto", Toast.LENGTH_SHORT).show()
                    pbLoading.visibility = View.GONE
                    llUserData.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "Error procesando imagen", Toast.LENGTH_SHORT).show()
                pbLoading.visibility = View.GONE
                llUserData.visibility = View.VISIBLE
            }
        }
    }

    private fun actualizarPassword() {
        val current = etCurrentPassword.text.toString()
        val newPass = etNewPassword.text.toString()
        val confirm = etConfirmPassword.text.toString()

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPass != confirm) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        btnActualizarPassword.isEnabled = false

        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.getApiService(this@DashboardActivity)
                val response = apiService.updatePassword(PasswordRequest(current, newPass, confirm))

                if (response.isSuccessful) {
                    Toast.makeText(this@DashboardActivity, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                    etCurrentPassword.text.clear()
                    etNewPassword.text.clear()
                    etConfirmPassword.text.clear()
                } else {
                    Toast.makeText(this@DashboardActivity, "Error. Verifica tu contraseña actual", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                btnActualizarPassword.isEnabled = true
            }
        }
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            try { RetrofitClient.getApiService(this@DashboardActivity).logout() } catch (e: Exception) { }
            tokenManager.clearToken()
            irAlLogin()
        }
    }

    private fun irAlLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}