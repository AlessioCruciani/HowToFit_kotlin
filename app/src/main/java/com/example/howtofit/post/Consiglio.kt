package com.example.howtofit.post

data class Consiglio(
    val uid: String?,
    val desc: String?,
    val dataEvento: String?,
    val temaEvento: String?,
    val dataCreazione: String?,
    val oraCreazione: String?,
    val idCommento: String
): Comparable<Consiglio> {

    override fun compareTo(other: Consiglio): Int {
        // Confronta prima le date di creazione
        val dataComparison = (this.dataCreazione ?: "").compareTo(other.dataCreazione ?: "")

        // Se le date sono uguali, confronta le ore di creazione
        return if (dataComparison == 0) {
            (this.oraCreazione ?: "").compareTo(other.oraCreazione ?: "")
        } else {
            dataComparison
        }
    }
}











