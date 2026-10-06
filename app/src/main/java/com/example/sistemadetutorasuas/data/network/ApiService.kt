package com.example.sistemadetutorasuas.data.network

import com.example.sistemadetutorasuas.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Obtener carreras activas
    @GET("carreras")
    suspend fun getCarreras(): Response<List<Carrera>>

    // Iniciar sesión
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Obtener usuario autenticado (Ruta protegida)
    @GET("user")
    suspend fun getUser(): Response<User>

    // Cerrar sesión (Ruta protegida)
    @POST("logout")
    suspend fun logout(): Response<Map<String, String>>
}