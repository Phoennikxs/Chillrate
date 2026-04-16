package com.example.chillrate.model

data class UserResponse(
    val id: Int,
    val email: String,
    val full_name: String,
    val sex: String? = null,
    val age: Int? = null,
    val height_cm: Int? = null,
    val weight_kg: Int? = null,
    val is_verified: Boolean = false
)