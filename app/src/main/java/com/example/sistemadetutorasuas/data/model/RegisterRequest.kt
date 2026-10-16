package com.example.sistemadetutorasuas.data.model

data class RegisterRequest (
    val nombre: String,
    val apellido: String,
    val correo_institucional: String,
    val password: String,
    val password_confirmation: String,
    val carrera: String
)