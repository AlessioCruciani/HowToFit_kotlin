package com.example.howtofit.post

data class Commento(
    val uid: String,
    val dataCreazione: String,
    val oraCreazione: String,
    val idCommento: String,
    val testo: String,
    val cappello: String,
    val maglietta: String,
    val pantalone: String,
    val giacca: String,
    val scarpe: String,
    val accessori: List<String>
)
