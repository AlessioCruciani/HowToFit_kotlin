package com.example.howtofit.post

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.howtofit.R

class FotoAdapter(private val fotoList: MutableList<Bitmap?>) : RecyclerView.Adapter<FotoAdapter.FotoViewHolder>() {

    private var itemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_foto, parent, false)
        return FotoViewHolder(view)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    override fun onBindViewHolder(holder: FotoViewHolder, position: Int) {
        val fotoBitmap = fotoList[position]
        if (fotoBitmap != null) {
            holder.imageViewFoto.setImageBitmap(fotoBitmap)
        } else {
            // Imposta un'immagine di default o una visualizzazione vuota, a seconda delle tue esigenze
            holder.imageViewFoto.setImageResource(R.drawable.userbasic)
        }

        holder.imageViewFoto.setOnClickListener {
            val currentPosition = holder.adapterPosition
            itemClickListener?.onItemClick(currentPosition)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun getItemCount(): Int {
        return fotoList.size
    }

    inner class FotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewFoto: ImageView = itemView.findViewById(R.id.imageViewFoto)
    }
}
