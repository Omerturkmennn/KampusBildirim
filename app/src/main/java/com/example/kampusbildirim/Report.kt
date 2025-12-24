package com.example.kampusbildirim

import com.google.firebase.Timestamp

data class Report(
    val description: String = "",
    val imageUrl: String = "",
    val userEmail: String = "",
    val userId: String = "",
    val timestamp: Timestamp? = null,

    //Konum Bilgileri
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0

)
