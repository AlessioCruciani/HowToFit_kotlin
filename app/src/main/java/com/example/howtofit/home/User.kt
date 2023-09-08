package com.example.howtofit.home

import android.graphics.Bitmap
import com.example.howtofit.R

data class User(
    val username: String = "",
    val nome: String = "",
    val cognome: String = "",
    var profileImage: Bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888),
    val mail: String = ""
)
