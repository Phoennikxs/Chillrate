package com.example.chillrate.model

data class GroupMember(
    val id: Int,
    val fullName: String,
    val heartRate: Int,
    val stressLevel: Int,           // в процентах
    val isOnline: Boolean = true
)