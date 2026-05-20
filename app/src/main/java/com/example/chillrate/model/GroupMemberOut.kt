package com.example.chillrate.model

data class GroupMemberOut(
    val id: Int,
    val full_name: String,
    val email: String,
    val heart_rate: Int? = null,
    val stress_level: Int? = null
)