package com.example.chillrate.model

data class UpdateUserParamsRequest(
    val email: String,
    val sex: String? = null,
    val age: Int? = null,
    val height_cm: Int? = null,
    val weight_kg: Int? = null
)