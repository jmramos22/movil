package com.example.sistemadetutorasuas.data.network

import com.example.sistemadetutorasuas.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {


    // Obtener carreras activas
    @GET("carreras")
    suspend fun getCarreras(): Response<List<Carrera>>

    // Iniciar sesión
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Registro de usuario
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Obtener usuario autenticado (Ruta protegida)
    @GET("user")
    suspend fun getUser(): Response<User>

    // Cerrar sesión (Ruta protegida)
    @POST("logout")
    suspend fun logout(): Response<Map<String, String>>

    // Actualizar contraseña
    @PUT("user/password")
    suspend fun updatePassword(@Body request: PasswordRequest): Response<Map<String, String>>

    // Subir imagen de perfil
    @Multipart
    @POST("user/image")
    suspend fun uploadProfileImage(@Part image: MultipartBody.Part): Response<Map<String, String>>

}