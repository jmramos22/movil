package com.example.sistemadetutorasuas.data.model

data class AuthResponse (

    val message: String,
    val token: String?,
    val user: User?,
    val errors: Map<String, List<String>>?
)