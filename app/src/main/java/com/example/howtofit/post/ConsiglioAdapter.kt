package com.example.howtofit.post

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.howtofit.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

interface ConsiglioItemClickListener {
     fun onAggiungiConsiglioClicked(uid: String?, idCommento: String)
}


class ConsiglioAdapter(private val cons: List<Consiglio>, private val itemClickListener: ConsiglioItemClickListener) :
    RecyclerView.Adapter<ConsiglioAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.consiglio_item, parent, false)
        val viewHolder = PostViewHolder(itemView)
        return viewHolder
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val consiglio = cons[position]
        holder.bind(consiglio)
    }

    override fun getItemCount() = cons.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewText: TextView = itemView.findViewById(R.id.textViewDescConsiglio)
        private val textViewAuthor: TextView = itemView.findViewById(R.id.textViewUsernameConsiglio)
        private val imagePostUser: ImageView = itemView.findViewById(R.id.imageConsiglioUser)
        private val dataEventoConsiglio: TextView = itemView.findViewById(R.id.textViewDataEventoConsiglio)
        private val temaEventoConsiglio: TextView = itemView.findViewById(R.id.textViewTemaEventoConsiglio)
        private val dataCreazioneConsiglio: TextView = itemView.findViewById(R.id.textViewDataCreazioneConsiglio)
        private val oraCreazioneConsiglio: TextView = itemView.findViewById(R.id.textViewOraCreazioneConsiglio)
        private val buttonAggiungiConsiglio: Button = itemView.findViewById(R.id.buttonAggiungiConsiglio)
        private val storage = FirebaseStorage.getInstance()

        fun bind(cons: Consiglio) {
            val firestore = FirebaseFirestore.getInstance()
            val postsCollection = firestore.collection("users")

            postsCollection.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.id == cons.uid) {
                        textViewAuthor.text = document.get("username").toString()

                        val storageRef = storage.reference
                        val nomeCartella = "${document.id}/profilo/immagine_base.jpg"
                        val imageRef = storageRef.child(nomeCartella)

                        imageRef.getBytes(1024 * 1024).addOnSuccessListener { bytes ->
                            val bitmap =
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size) as Bitmap
                            imagePostUser.setImageBitmap(bitmap)
                        }
                    }
                }
            }

            buttonAggiungiConsiglio.setOnClickListener {
                itemClickListener.onAggiungiConsiglioClicked(cons.uid, cons.idCommento)
                val context = itemView.context
            }

            textViewText.text = cons.desc
            dataEventoConsiglio.text = cons.dataEvento
            temaEventoConsiglio.text = cons.temaEvento
            oraCreazioneConsiglio.text = cons.oraCreazione
            dataCreazioneConsiglio.text = cons.dataCreazione
        }
    }


}
