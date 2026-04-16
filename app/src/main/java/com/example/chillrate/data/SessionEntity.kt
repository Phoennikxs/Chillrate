package com.example.chillrate.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val userEmail: String,
    val startTime: Date,
    val endTime: Date,
    val durationSeconds: Int,
    val averageHR: Int,
    val maxHR: Int,
    val stressLevel: Float? = null,
    val notes: String? = null,


    val hrDataJson: String = "",

    val createdAt: Date = Date()
)