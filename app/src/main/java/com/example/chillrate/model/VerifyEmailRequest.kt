package com.example.chillrate.model

data class VerifyEmailRequest(
    val email: String,
    val code: String
)