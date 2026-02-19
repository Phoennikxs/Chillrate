package com.example.chillrate.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String
)
