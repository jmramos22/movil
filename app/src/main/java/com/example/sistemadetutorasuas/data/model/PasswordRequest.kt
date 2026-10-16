package com.example.sistemadetutorasuas.data.model

data class PasswordRequest (
    val current_password: String,
    val password: String,
    val password_confirmation: String
)