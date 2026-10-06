package com.example.sistemadetutorasuas.data.model

data class User (
    val id: Int,
    val nombre: String,
    val apellido: String,
    val correo_institucional: String,
    val carrera: String,
    val rol: String
)