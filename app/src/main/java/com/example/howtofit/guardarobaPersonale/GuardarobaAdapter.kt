package com.example.howtofit.guardarobaPersonale

import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.howtofit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage


class GuardarobaAdapter(private val imageList: List<String>, private val deleteButton: Button) :
    RecyclerView.Adapter<GuardarobaAdapter.ImageViewHolder>() {

    private val longClickPositions = HashSet<Int>()
    private var isDeleteButtonVisible = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery, parent, false)
        Log.e("gregrerg", deleteButton.toString())
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        val imageView = holder.imageView
        val deleteIcon = holder.deleteIcon
        Glide.with(holder.itemView)
            .load(imageUrl)
            .into(holder.imageView)
        holder.imageView.contentDescription = imageUrl

        if (longClickPositions.contains(position)) {
            deleteIcon.visibility = View.VISIBLE
        } else {
            deleteIcon.visibility = View.INVISIBLE
        }

        deleteButton.setOnClickListener {
            deleteImagesFromStorage(longClickPositions)
            longClickPositions.clear()
            deleteButton.visibility = View.GONE
        }

        imageView.setOnClickListener{
            if (longClickPositions.contains(position)){
                longClickPositions.remove(position)
                deleteIcon.visibility = View.INVISIBLE
                deleteButton.text = "Delete Selected (${longClickPositions.size})"
                updateDeleteButtonVisibility()
            }
        }

        imageView.setOnLongClickListener{
            // Avvia un conteggio del tempo quando il click a lungo termine inizia
            val startTime = System.currentTimeMillis()

            // Utilizza un Handler per ritardare l'azione dopo due secondi
            val handler = Handler()
            handler.postDelayed({
                // Verifica se l'immagine è ancora in stato di click a lungo termine
                if (System.currentTimeMillis() - startTime >= 1000) {
                    // L'immagine è stata cliccata per più di due secondi
                    // Esegui qui l'azione desiderata
                    Log.e("2secondi", imageView.contentDescription.toString())

                    longClickPositions.add(position)

                    deleteIcon.visibility = View.VISIBLE
                    deleteButton.text = "Delete Selected (${longClickPositions.size})"
                    updateDeleteButtonVisibility()

                }
            }, 1000)

            true // Segnala che l'evento di click a lungo termine è stato gestito
        }

    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_viewX)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
    }

    private fun updateDeleteButtonVisibility() {
        if (longClickPositions.isEmpty()) {
            deleteButton.visibility = View.GONE
        } else {
            deleteButton.visibility = View.VISIBLE
        }
    }

    private fun deleteImagesFromStorage(positions: Set<Int>) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        for (position in positions) {
            val imageUrl = imageList[position]
            val imageRef = storage.getReferenceFromUrl(imageUrl)

            // Elimina l'immagine dallo Storage
            imageRef.delete()
                .addOnSuccessListener {
                    // L'immagine è stata eliminata con successo dallo Storage
                    // Ora puoi rimuovere l'immagine dall'elenco locale e aggiornare l'adattatore
                    imageList.drop(position)
                    notifyItemRemoved(position)
                }
                .addOnFailureListener { exception ->
                    // Gestione dell'errore
                    Log.e("StorageDeleteError", "Errore nell'eliminazione dell'immagine: $exception")
                }
        }
    }
}

