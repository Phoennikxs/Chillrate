package com.example.chillrate.model

data class GroupMember(
    val id: Int,
    val fullName: String,
    val email: String = "",
    val heartRate: Int = 0,
    val stressLevel: Int = 0,
    val isOnline: Boolean = true
)