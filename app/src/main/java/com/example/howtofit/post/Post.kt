package com.example.howtofit.post

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.Task
import java.util.Date

data class Post(
    val uid: String,
    val desc: String,
    val date: Date,
    val postId: String,
    val time: Date,
    val scelte: List<Bitmap>
){
    var selectedIndex: Int = -1 //

    // Metodo per il confronto tra oggetti Post in base alla data
    fun compareTo(other: Post): Int {
        return this.date.compareTo(other.date)
        return this.time.compareTo(other.time)
    }
}






