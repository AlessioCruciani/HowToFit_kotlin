package com.example.howtofit.post

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.howtofit.R
import com.google.android.gms.common.util.MurmurHash3
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*


lateinit var popupWindow: PopupWindow
private lateinit var uidCreatore: String
private lateinit var idCommento: String
private lateinit var clickedImageUrl: String
private var onImageClickListener: inserisciCommento.OnImageClickListener? = null
//CAPPELLI
private lateinit var cardViewButtonAddCappello: CardView
private lateinit var buttonAddCappello: ImageButton
private lateinit var cardViewImageCappello: CardView
private lateinit var imageViewCappello: ImageView
//MAGLIETTE
private lateinit var cardViewButtonAddMagliette: CardView
private lateinit var buttonAddMagliette: ImageButton
private lateinit var cardViewImageMaglietta: CardView
private lateinit var imageViewMaglietta: ImageView
//GIACCHE
private lateinit var cardViewButtonAddGiacche: CardView
private lateinit var buttonAddGiacche: ImageButton
private lateinit var cardViewImageGiacca: CardView
private lateinit var imageViewGiacca: ImageView
//PANTALONI
private lateinit var cardViewButtonAddPantaloni: CardView
private lateinit var buttonAddPantaloni: ImageButton
private lateinit var cardViewImagePantalone: CardView
private lateinit var imageViewPantalone: ImageView
//SCARPE
private lateinit var cardViewButtonAddScarpe: CardView
private lateinit var buttonAddScarpe: ImageButton
private lateinit var cardViewImageScarpe: CardView
private lateinit var imageViewScarpe: ImageView
//ACCESSORI
private lateinit var accessoriContainer: LinearLayout
private lateinit var cardViewButtonAddAccessori: CardView
private lateinit var buttonAddAccessori: ImageButton
private lateinit var cardViewImageAccessorio: CardView
private lateinit var imageViewAccessorio: ImageView

private lateinit var editTextMessaggio: EditText

private var maglietta: String = "Non inserito"
private var cappello: String = "Non inserito"
private var giacca: String = "Non inserito"
private var scarpe: String = "Non inserito"
private var pantalone: String = "Non inserito"
private var accessori = mutableListOf<String>()

private val auth: FirebaseAuth = FirebaseAuth.getInstance()
val user = auth.currentUser
private lateinit var firestore: FirebaseFirestore

class inserisciCommento : AppCompatActivity() {

    interface OnImageClickListener {
        fun onImageClick(imageUrl: String, categoria: String)
    }

    fun setImageClickListener(listener: OnImageClickListener) {
        onImageClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inserisci_commento)
        uidCreatore = intent.getStringExtra("uidCreatore").toString()
        idCommento = intent.getStringExtra("idCommento").toString()

        val buttonGoBack: ImageButton = findViewById(R.id.buttonGoBack)
        buttonGoBack.setOnClickListener{
            finish()
            }
        editTextMessaggio = findViewById(R.id.testoDelCommento)

        //CAPPELLI
        cardViewButtonAddCappello = findViewById(R.id.cardViewButtonAddCappelli)
        buttonAddCappello = findViewById(R.id.buttonAddCappelli)
        cardViewImageCappello = findViewById(R.id.cardViewImageCappello)
        imageViewCappello = findViewById(R.id.imageViewCappello)
        //MAGLIETTE
        cardViewButtonAddMagliette = findViewById(R.id.cardViewButtonAddMagliette)
        buttonAddMagliette = findViewById(R.id.buttonAddMagliette)
        cardViewImageMaglietta = findViewById(R.id.cardViewImageMaglietta)
        imageViewMaglietta = findViewById(R.id.imageViewMaglietta)
        //GIACCHE
        cardViewButtonAddGiacche = findViewById(R.id.cardViewButtonAddGiacche)
        buttonAddGiacche = findViewById(R.id.buttonAddGiacche)
        cardViewImageGiacca = findViewById(R.id.cardViewImageGiacca)
        imageViewGiacca = findViewById(R.id.imageViewGiacca)
        //PANTALONI
        cardViewButtonAddPantaloni = findViewById(R.id.cardViewButtonAddPantaloni)
        buttonAddPantaloni = findViewById(R.id.buttonAddPantaloni)
        cardViewImagePantalone = findViewById(R.id.cardViewImagePantalone)
        imageViewPantalone = findViewById(R.id.imageViewPantalone)
        //SCARPE
        cardViewButtonAddScarpe = findViewById(R.id.cardViewButtonAddScarpe)
        buttonAddScarpe = findViewById(R.id.buttonAddScarpe)
        cardViewImageScarpe = findViewById(R.id.cardViewImageScarpe)
        imageViewScarpe = findViewById(R.id.imageViewScarpe)
        //ACCESSORI
        accessoriContainer = findViewById(R.id.accessoriContainer)
        cardViewButtonAddAccessori = findViewById(R.id.cardViewButtonAddAccessori)
        buttonAddAccessori = findViewById(R.id.buttonAddAccessori)
        cardViewImageAccessorio = findViewById(R.id.cardViewImageAccessorio)
        imageViewAccessorio = findViewById(R.id.imageViewAccessorio)

        val buttonInserisciCommento: Button = findViewById(R.id.buttonInserisciCommento)
        buttonInserisciCommento.setOnClickListener{
            creaCommento()
        }

        val inserisciCommento = inserisciCommento()
        inserisciCommento.setImageClickListener(object : OnImageClickListener {
            override fun onImageClick(imageUrl: String, categoria: String) {

                when (categoria){
                    "cappelli" ->{
                        cardViewButtonAddCappello.visibility = View.GONE
                        val requestOptions = RequestOptions().centerCrop()
                        cappello = imageUrl
                        Glide.with(this@inserisciCommento)
                            .load(imageUrl)
                            .apply(requestOptions)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageViewCappello)
                        cardViewImageCappello.visibility = View.VISIBLE
                    }
                    "magliette" ->{
                        cardViewButtonAddMagliette.visibility = View.GONE
                        val requestOptions = RequestOptions().centerCrop()
                        maglietta = imageUrl
                        Glide.with(this@inserisciCommento)
                            .load(imageUrl)
                            .apply(requestOptions)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageViewMaglietta)
                        cardViewImageMaglietta.visibility = View.VISIBLE
                    }
                    "giacche" ->{
                        cardViewButtonAddGiacche.visibility = View.GONE
                        val requestOptions = RequestOptions().centerCrop()
                        giacca = imageUrl
                        Glide.with(this@inserisciCommento)
                            .load(imageUrl)
                            .apply(requestOptions)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageViewGiacca)
                        cardViewImageGiacca.visibility = View.VISIBLE
                    }
                    "pantaloni" ->{
                        cardViewButtonAddPantaloni.visibility = View.GONE
                        val requestOptions = RequestOptions().centerCrop()
                        pantalone = imageUrl
                        Glide.with(this@inserisciCommento)
                            .load(imageUrl)
                            .apply(requestOptions)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageViewPantalone)
                        cardViewImagePantalone.visibility = View.VISIBLE
                    }
                    "scarpe" ->{
                        cardViewButtonAddScarpe.visibility = View.GONE
                        val requestOptions = RequestOptions().centerCrop()
                        scarpe = imageUrl
                        Glide.with(this@inserisciCommento)
                            .load(imageUrl)
                            .apply(requestOptions)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageViewScarpe)
                        cardViewImageScarpe.visibility = View.VISIBLE
                    }
                    "accessori" ->{
                        if (accessoriContainer.childCount == 11){
                            cardViewButtonAddAccessori.visibility = View.GONE
                        }

                        val imageViewAccessorioDinamic = ImageView(this@inserisciCommento)
                        imageViewAccessorioDinamic.layoutParams = imageViewAccessorio.layoutParams

                        val cardViewAccessorioDinamic = CardView(this@inserisciCommento)
                        cardViewAccessorioDinamic.layoutParams = cardViewImageAccessorio.layoutParams
                        cardViewAccessorioDinamic.radius = cardViewImageAccessorio.radius
                        cardViewAccessorioDinamic.foregroundGravity = cardViewImageAccessorio.foregroundGravity
                        cardViewAccessorioDinamic.contentDescription = imageUrl
                        cardViewAccessorioDinamic.addView(imageViewAccessorioDinamic)

                        accessoriContainer.addView(cardViewAccessorioDinamic)

                        cardViewAccessorioDinamic.setOnClickListener{
                            accessori.remove(cardViewAccessorioDinamic.contentDescription.toString())
                            accessoriContainer.removeView(cardViewAccessorioDinamic)
                            cardViewButtonAddAccessori.visibility = View.VISIBLE
                        }

                        accessori.add(imageUrl)

                        val requestOptions = RequestOptions().centerCrop()
                        Glide.with(this@inserisciCommento)
                            .load(imageUrl)
                            .apply(requestOptions)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageViewAccessorioDinamic)
                        cardViewAccessorioDinamic.visibility = View.VISIBLE
                    }
                }
            }
        })
        //CAPPELLI
        buttonAddCappello.setOnClickListener(){
            openPopupWindow("cappelli")
        }
        cardViewImageCappello.setOnClickListener {
            imageViewCappello.setImageBitmap(null)
            cardViewImageCappello.visibility = View.GONE
            cardViewButtonAddCappello.visibility = View.VISIBLE
        }
        //MAGLIETTE
        buttonAddMagliette.setOnClickListener(){
            openPopupWindow("magliette")
        }
        cardViewImageMaglietta.setOnClickListener {
            imageViewMaglietta.setImageBitmap(null)
            cardViewImageMaglietta.visibility = View.GONE
            cardViewButtonAddMagliette.visibility = View.VISIBLE
        }
        //GIACCHE
        buttonAddGiacche.setOnClickListener(){
            openPopupWindow("giacche")
        }
        cardViewImageGiacca.setOnClickListener {
            imageViewGiacca.setImageBitmap(null)
            cardViewImageGiacca.visibility = View.GONE
            cardViewButtonAddGiacche.visibility = View.VISIBLE
        }
        //PANTALONI
        buttonAddPantaloni.setOnClickListener(){
            openPopupWindow("pantaloni")
        }
        cardViewImagePantalone.setOnClickListener {
            imageViewPantalone.setImageBitmap(null)
            cardViewImagePantalone.visibility = View.GONE
            cardViewButtonAddPantaloni.visibility = View.VISIBLE
        }
        //SCARPE
        buttonAddScarpe.setOnClickListener(){
            openPopupWindow("scarpe")
        }
        cardViewImageScarpe.setOnClickListener {
            imageViewScarpe.setImageBitmap(null)
            cardViewImageScarpe.visibility = View.GONE
            cardViewButtonAddScarpe.visibility = View.VISIBLE
        }
        //ACCESSORI
        buttonAddAccessori.setOnClickListener(){
            openPopupWindow("accessori")
        }
        cardViewImageAccessorio.setOnClickListener {
            imageViewAccessorio.setImageBitmap(null)
            cardViewImageAccessorio.visibility = View.GONE
            cardViewButtonAddAccessori.visibility = View.VISIBLE
        }

        //COMMENTO

    }
    fun creaCommento(){

        val testo = editTextMessaggio.text.toString()

        if (cappello == null){
            cappello = "Non inserito"
        }
        if (maglietta == null){
            maglietta = "Non inserito"
        }
        if (giacca == null){
            giacca = "Non inserito"
        }
        if (pantalone == null){
            pantalone = "Non inserito"
        }
        if (scarpe == null){
            scarpe = "Non inserito"
        }
        if (accessori == null){
            accessori = mutableListOf()
        }
        if (testo.isEmpty() && cappello == "Non inserito" && maglietta == "Non inserito" && giacca == "Non inserito" && pantalone == "Non inserito" && scarpe == "Non inserito" && accessori.isEmpty()){
            Toast.makeText(this, "Non puoi inserire un commento vuoto", Toast.LENGTH_LONG).show()
        }
        else{
            val sondaggioData = hashMapOf(
                "uid" to user?.uid,
                "data" to getCurrentDate(),
                "ora" to getCurrentTime(),
                "idCommento" to idCommento,
                "testo" to testo,
                "cappello" to cappello,
                "maglietta" to maglietta,
                "giacca" to giacca,
                "pantalone" to pantalone,
                "scarpe" to scarpe,
                "accessori" to accessori.joinToString(","),
            )

            firestore = FirebaseFirestore.getInstance()

            firestore.collection("commenti")
                .add(sondaggioData)
                .addOnSuccessListener {
                }
            finish()
        }


    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    // Funzione di utilità per ottenere l'ora corrente nel formato desiderato
    fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date()
        return timeFormat.format(date)
    }

    fun openPopupWindow(categoria: String) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.image_picker, null)
        // Imposta le dimensioni desiderate per il popup
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT

        // Crea la finestra di popup
        popupWindow = PopupWindow(popupView, width, height, true)

        // Imposta una transizione di animazione per l'apertura del popup
        popupWindow.animationStyle = R.style.PopupAnimation

        // Ottieni la vista del pulsante "X" dal layout del popup
        val closeButton: ImageView = popupView.findViewById(R.id.closeButtonImagePicker)

        // Gestisci il clic sul pulsante "X" per chiudere il popup
        closeButton.setOnClickListener {
            popupWindow.dismiss()
        }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val folderRef = storageRef.child("/$uidCreatore/guardaroba/$categoria")

        folderRef.listAll().addOnSuccessListener { listResult ->
            val cardView: CardView = popupView.findViewById<CardView>(R.id.cardViewImmagineCommento)
            val imageView: ImageView = popupView.findViewById<ImageView>(R.id.imageViewImmagineCommento)

            val imageContainer: LinearLayout = popupView.findViewById(R.id.container)

            val imageTasks = mutableListOf<Task<Uri>>()

            for (item in listResult.items) {
                // Ottieni l'URI dell'immagine corrente come Task<Uri>
                val imageUriTask = item.downloadUrl
                imageTasks.add(imageUriTask)
            }

            Tasks.whenAllSuccess<Uri>(imageTasks).addOnSuccessListener { uriList ->
                for (uri in uriList) {
                    val newImageView = ImageView(this)
                    newImageView.layoutParams = imageView.layoutParams
                    newImageView.contentDescription = uri.toString()

                    val newCardView = CardView(this)
                    newCardView.layoutParams = cardView.layoutParams
                    newCardView.radius = cardView.radius
                    newCardView.foregroundGravity = cardView.foregroundGravity
                    newCardView.contentDescription = uri.toString()
                    newCardView.addView(newImageView)

                    val imageUrl = uri.toString()

                    val requestOptions = RequestOptions().centerCrop()

                    Glide.with(this)
                        .load(imageUrl)
                        .apply(requestOptions)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(newImageView)

                    newImageView.setOnClickListener {
                        // Gestisci il clic sull'immagine qui
                        clickedImageUrl = newImageView.contentDescription.toString()
                        onImageClickListener?.onImageClick(clickedImageUrl, categoria)
                        popupWindow.dismiss()
                    }


                    if (newCardView.contentDescription.toString() in accessori){
                    }else{
                        imageContainer.addView(newCardView)
                    }
                }
            }.addOnFailureListener { exception ->
                // Gestisci eventuali errori nell'ottenere gli URI delle immagini da Firebase Storage
                // Log o mostra un messaggio di errore appropriato
            }
        }.addOnFailureListener { exception ->
            // Gestisci eventuali errori nell'ottenere gli URI delle immagini da Firebase Storage
            // Log o mostra un messaggio di errore appropriato
        }






        val rootView: View = findViewById(android.R.id.content)
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)

    }
}