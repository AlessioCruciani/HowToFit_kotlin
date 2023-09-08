package com.example.howtofit.home

import PostAdapter
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.howtofit.R
import com.example.howtofit.guardarobaPersonale.GalleryAdapter
import com.example.howtofit.guardarobaPersonale.GuardarobaAdapter
import com.example.howtofit.post.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class UserAccountFragment : Fragment(), ConsiglioItemClickListener {
    private var username: String? = null
    private var uid: String? = null
    private val storage = FirebaseStorage.getInstance()
    private lateinit var galleryAdapter: GalleryAdapter
    private val abbigliamento =
        listOf("cappelli", "magliette", "giacche", "pantaloni", "scarpe", "accessori")
    private lateinit var postAdapter: PostAdapter
    private lateinit var consiglioAdapter: ConsiglioAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager

    companion object {
        const val TAG = "UserAccountFragment"
        private const val NUM_COLUMNS_GALLERY = 2
        private const val NUM_COLUMNS_POST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_account_clicked, container, false)

        arguments?.let {
            username = it.getString("username")
        }
        val firestore = FirebaseFirestore.getInstance()
        val utentiCollection = firestore.collection("users")

        utentiCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    if (document.getString("username").toString() == username) {
                        uid = document.id
                    }
                }
            }

            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting comments: ${exception.message}")
            }

        val fotoButton = view.findViewById<ImageButton>(R.id.fotoButton)
        val sondaggioButton = view.findViewById<ImageButton>(R.id.sondaggioButton)
        val consiglioButton = view.findViewById<ImageButton>(R.id.consigliButton)
        val giallo = ContextCompat.getColor(requireContext(), R.color.IlluminatigYellow)
        val bianco = ContextCompat.getColor(requireContext(), R.color.white)
        val accountUsername: TextView = view.findViewById(R.id.textView)
        val immagineProfilo: ImageView = view.findViewById(R.id.imageProfilo)
        val accountNome: TextView = view.findViewById(R.id.nome)
        val accountCognome: TextView = view.findViewById(R.id.cognome)
        val accountTelefono: TextView = view.findViewById(R.id.telefono)
        val accountBiografia: TextView = view.findViewById(R.id.biografia)
        recyclerView = view.findViewById(R.id.gallery_recycler_view)

        gridLayoutManager = GridLayoutManager(requireContext(), NUM_COLUMNS_GALLERY)
        linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val db: FirebaseFirestore = Firebase.firestore
        val raccoltaUtenti = db.collection("users")
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference
        var idUtente: String = ""

        raccoltaUtenti.whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    idUtente = document.id
                    Log.e(TAG,"$idUtente")
                    val username = document.getString("username")
                    val nome = document.getString("nome")
                    val cognome = document.getString("cognome")
                    val telefono = document.getString("telefono")
                    val biografia = document.getString("biografia")
                    accountUsername.text = username
                    accountNome.text = nome
                    accountCognome.text = cognome
                    accountTelefono.text = telefono
                    accountBiografia.text = biografia

                    val nomeCartella = "$uid/profilo/immagine_base.jpg"
                    val imageRef: StorageReference = storageRef.child(nomeCartella)

                    val imageList = getImageList(idUtente)
                    galleryAdapter = GalleryAdapter(imageList)
                    recyclerView.adapter = galleryAdapter

                    val ONE_MEGABYTE = 1024 * 1024.toLong()
                    imageRef.getBytes(ONE_MEGABYTE)
                        .addOnSuccessListener { bytes ->
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            immagineProfilo.setImageBitmap(bitmap)
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error downloading image: ${exception.message}")
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting user with specified username: ${exception.message}")

            }

        fotoButton.setOnClickListener {
            recyclerView.adapter = galleryAdapter
            recyclerView.layoutManager = gridLayoutManager
            fotoButton.setColorFilter(giallo)
            sondaggioButton.setColorFilter(bianco)
            consiglioButton.setColorFilter(bianco)
        }
        sondaggioButton.setOnClickListener {
            val postList = getPostList(username.toString())
            postAdapter = PostAdapter(postList)
            recyclerView.adapter = postAdapter
            recyclerView.layoutManager = linearLayoutManager
            sondaggioButton.setColorFilter(giallo)
            fotoButton.setColorFilter(bianco)
            consiglioButton.setColorFilter(bianco)
        }
        consiglioButton.setOnClickListener {
            val consigliList = getConsigliList(username.toString())
            consiglioAdapter = ConsiglioAdapter(consigliList, this)
            recyclerView.adapter = consiglioAdapter
            recyclerView.layoutManager = linearLayoutManager
            consiglioAdapter.notifyDataSetChanged()
            consiglioButton.setColorFilter(giallo)
            sondaggioButton.setColorFilter(bianco)
            fotoButton.setColorFilter(bianco)
        }

        return view
    }

    private fun getImageList(id: String): List<String> {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageUrls = mutableListOf<String>()

        val db = Firebase.firestore
        //val raccoltaUtenti = db.collection("users")

        for (i in abbigliamento) {
            val folderRef = storageRef.child("$id/guardaroba/$i")
            Log.e("bestemmia","$id/guardaroba/$i")
            folderRef.listAll()
                .addOnSuccessListener { listResult ->
                    for (item in listResult.items) {
                        item.downloadUrl
                            .addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()
                                imageUrls.add(imageUrl)
                                galleryAdapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { exception ->
                                Log.e(
                                    "Immagini non caricate",
                                    "l'url dell'immagine non è stato inserito nella lista"
                                )
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Immagini non ottenute", "Gli url delle immagini non sono stati ottenuti")
                }
        }

        return imageUrls
    }

    private fun getPostList(username: String): List<Post> {
        val storage = FirebaseStorage.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val postsCollection = firestore.collection("sondaggi")
        val postsList = mutableListOf<Post>()

        val query = postsCollection.whereEqualTo("uid", uid)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val uid = document.getString("uid").toString()
                    val desc = document.getString("desc").toString()
                    val date = document.getString("date").toString()
                    val time = document.getString("time").toString()
                    val scelte = document.get("img").toString()
                    val postId = document.get("postId").toString()

                    val data = convertStringToDate(date)
                    val ora = convertStringToTime(time)

                    var lista: List<String> = emptyList()
                    lista =
                        scelte.split(",") // Dividi la stringa utilizzando la virgola come delimitatore
                    lista = lista.drop(1)

                    val bitmapList: MutableList<Bitmap> = mutableListOf()

                    runBlocking {
                        for (nomeImmagine in lista) {
                            val storageRef = storage.reference
                            val nomeCartella = "/sondaggi/${document.id}/$nomeImmagine"
                            val imageRef = storageRef.child(nomeCartella)

                            val downloadTask = async(Dispatchers.IO) {
                                try {
                                    val bytes = imageRef.getBytes(5 * 1024 * 1024).await()
                                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    bitmapList.add(bitmap)
                                } catch (e: Exception) {
                                    Log.e(
                                        ContentValues.TAG,
                                        "Error downloading image: ${e.message}"
                                    )
                                }
                            }

                            downloadTask.await()
                        }
                    }

                    val post = Post(uid, desc, data, postId, ora, bitmapList)
                    postsList.add(post)
                }


                postAdapter = PostAdapter(postsList)
                recyclerView.adapter = postAdapter
                postAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting posts: ${exception.message}")
            }

        return postsList
    }

    private fun getConsigliList(username: String): List<Consiglio> {
        val firestore = FirebaseFirestore.getInstance()
        val consigliCollection = firestore.collection("consigli")
        val consigliList = mutableListOf<Consiglio>()

        // Query per ottenere i consigli associati all'utente con il determinato username
        val query = consigliCollection.whereEqualTo("uid", uid)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val uid = document.getString("uid").toString()
                    val desc = document.getString("desc").toString()
                    val dataEvento = document.getString("dataEvento").toString()
                    val temeEvento = document.getString("temaEvento").toString()
                    val dataCreazione = document.getString("dataCreazione").toString()
                    val oraCreazione = document.getString("oraCreazione").toString()

                    val consiglio = Consiglio(
                        uid,
                        desc,
                        dataEvento,
                        temeEvento,
                        dataCreazione,
                        oraCreazione,
                        document.id
                    )
                    consigliList.add(consiglio)
                }

                // Aggiorna l'adapter con la lista dei consigli
                consiglioAdapter = ConsiglioAdapter(consigliList, this)
                recyclerView.adapter = consiglioAdapter
                consiglioAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting consigli: ${exception.message}")
            }

        return consigliList
    }

    override fun onAggiungiConsiglioClicked(uid: String?, idCommento: String) {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_commenti, null)
        val buttonCommenta = popupView.findViewById<Button>(R.id.buttonCommentaPost)

        // Imposta le dimensioni desiderate per il popup
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT

        // Crea la finestra di popup
        popupWindow = PopupWindow(popupView, width, height, true)

        if (uid == user?.uid) {
            buttonCommenta.visibility = View.GONE
        } else {
            buttonCommenta.visibility = View.VISIBLE
        }


        // Imposta una transizione di animazione per l'apertura del popup
        popupWindow.animationStyle = R.style.PopupAnimation

        // Ottieni la vista del pulsante "X" dal layout del popup
        val closeButton: ImageView = popupView.findViewById(R.id.closeButton)
        val linearCommenti: LinearLayout = popupView.findViewById(R.id.commentiUtentiLinear)

        //DAI DOCUMENTI IN FIREBASE PRENDI TUTTI QUELLI CHE HANNO L'ID DEL COMMENTO SELEZIONATO
        //E LI SALVI IN UNA LISTA DI COMMENTI

        getCommentiFromFirebase(idCommento) { commentiList ->
            for (commento in commentiList) {
                val itemCommento = inflater.inflate(R.layout.item_commento, null)

                val textViewAuthor = itemCommento.findViewById<TextView>(R.id.textViewAuthor)
                val textViewCommento = itemCommento.findViewById<TextView>(R.id.textViewCommento)
                //val horizontalScrollView = itemCommento.findViewById<HorizontalScrollView>(R.id.horizontalImmaginiConsiglio)
                val linearLayoutImmaginiConsiglio =
                    itemCommento.findViewById<LinearLayout>(R.id.linearLayoutImmaginiConsiglio)
                val cardViewImmagineConsiglio =
                    itemCommento.findViewById<CardView>(R.id.cardViewImageCommento)
                val imageView = itemCommento.findViewById<ImageView>(R.id.imageViewCommento)
                val textViewData =
                    itemCommento.findViewById<TextView>(R.id.textViewDataCreazioneCommento)
                val textViewOra =
                    itemCommento.findViewById<TextView>(R.id.textViewOraCreazioneCommento)
                val imageCommentoUser = itemCommento.findViewById<ImageView>(R.id.imageCommentoUser)


                val firestore = FirebaseFirestore.getInstance()
                val userCollection = firestore.collection("users")

                userCollection.get().addOnSuccessListener { documents ->
                    for (document in documents) {
                        if (document.id == commento.uid) {
                            textViewAuthor.text = document.get("username").toString()

                            val storageRef = storage.reference
                            val nomeCartella = "${document.id}/profilo/immagine_base.jpg"
                            val imageRef = storageRef.child(nomeCartella)

                            imageRef.getBytes(1024 * 1024).addOnSuccessListener { bytes ->
                                val bitmap =
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size) as Bitmap
                                imageCommentoUser.setImageBitmap(bitmap)
                            }
                        }
                    }
                }


                // Imposta i valori per gli elementi di layout
                textViewCommento.text = commento.testo
                textViewData.text = commento.dataCreazione
                textViewOra.text = commento.oraCreazione

                if (commento.cappello != "Non inserito") {
                    val imageViewCappello = ImageView(requireContext())
                    imageViewCappello.layoutParams = imageView.layoutParams
                    val cardViewImmagineCappello = CardView(requireContext())
                    cardViewImmagineCappello.layoutParams = cardViewImmagineConsiglio.layoutParams
                    cardViewImmagineCappello.radius = cardViewImmagineConsiglio.radius

                    Glide.with(this)
                        .load(commento.cappello)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageViewCappello)

                    cardViewImmagineCappello.addView(imageViewCappello)
                    linearLayoutImmaginiConsiglio.addView(cardViewImmagineCappello)
                }
                if (commento.maglietta != "Non inserito") {
                    val imageViewMaglietta = ImageView(requireContext())
                    imageViewMaglietta.layoutParams = imageView.layoutParams
                    val cardViewImmagineMaglietta = CardView(requireContext())
                    cardViewImmagineMaglietta.layoutParams = cardViewImmagineConsiglio.layoutParams
                    cardViewImmagineMaglietta.radius = cardViewImmagineConsiglio.radius

                    Glide.with(this)
                        .load(commento.maglietta)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageViewMaglietta)

                    cardViewImmagineMaglietta.addView(imageViewMaglietta)
                    linearLayoutImmaginiConsiglio.addView(cardViewImmagineMaglietta)
                }
                if (commento.giacca != "Non inserito") {
                    val imageViewGiacca = ImageView(requireContext())
                    imageViewGiacca.layoutParams = imageView.layoutParams
                    val cardViewImmagineGiacca = CardView(requireContext())
                    cardViewImmagineGiacca.layoutParams = cardViewImmagineConsiglio.layoutParams
                    cardViewImmagineGiacca.radius = cardViewImmagineConsiglio.radius

                    Glide.with(this)
                        .load(commento.giacca)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageViewGiacca)

                    cardViewImmagineGiacca.addView(imageViewGiacca)
                    linearLayoutImmaginiConsiglio.addView(cardViewImmagineGiacca)
                }
                if (commento.pantalone != "Non inserito") {
                    val imageViewPantalone = ImageView(requireContext())
                    imageViewPantalone.layoutParams = imageView.layoutParams
                    val cardViewImmaginePantalone = CardView(requireContext())
                    cardViewImmaginePantalone.layoutParams = cardViewImmagineConsiglio.layoutParams
                    cardViewImmaginePantalone.radius = cardViewImmagineConsiglio.radius

                    Glide.with(this)
                        .load(commento.pantalone)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageViewPantalone)

                    cardViewImmaginePantalone.addView(imageViewPantalone)
                    linearLayoutImmaginiConsiglio.addView(cardViewImmaginePantalone)
                }
                if (commento.scarpe != "Non inserito") {
                    val imageViewScarpe = ImageView(requireContext())
                    imageViewScarpe.layoutParams = imageView.layoutParams
                    val cardViewImmagineScarpe = CardView(requireContext())
                    cardViewImmagineScarpe.layoutParams = cardViewImmagineConsiglio.layoutParams
                    cardViewImmagineScarpe.radius = cardViewImmagineConsiglio.radius

                    Glide.with(this)
                        .load(commento.scarpe)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageViewScarpe)

                    cardViewImmagineScarpe.addView(imageViewScarpe)
                    linearLayoutImmaginiConsiglio.addView(cardViewImmagineScarpe)
                }
                if (commento.accessori.toString() != "[]") {
                    for (accessorio in commento.accessori) {
                        val imageViewAccessorio = ImageView(requireContext())
                        imageViewAccessorio.layoutParams = imageView.layoutParams
                        val cardViewImmagineAccessorio = CardView(requireContext())
                        cardViewImmagineAccessorio.layoutParams =
                            cardViewImmagineConsiglio.layoutParams
                        cardViewImmagineAccessorio.radius = cardViewImmagineConsiglio.radius

                        Glide.with(this)
                            .load(accessorio)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageViewAccessorio)

                        cardViewImmagineAccessorio.addView(imageViewAccessorio)
                        linearLayoutImmaginiConsiglio.addView(cardViewImmagineAccessorio)
                    }
                }


                // Aggiungi l'elemento di layout al container (scrollviewCommenti)
                linearCommenti.addView(itemCommento)
            }
        }

        // Gestisci il clic sul pulsante "X" per chiudere il popup
        closeButton.setOnClickListener {
            popupWindow.dismiss()
        }
        buttonCommenta.setOnClickListener {
            val intent = Intent(requireContext(), inserisciCommento::class.java)
            intent.putExtra("uidCreatore", uid)
            intent.putExtra("idCommento", idCommento)
            popupWindow.dismiss()
            startActivity(intent)
        }
        // Mostra il popup nella posizione desiderata
        popupWindow.showAtLocation(recyclerView, Gravity.CENTER, 0, 0)
    }

    fun getCommentiFromFirebase(idConsiglio: String, callback: (MutableList<Commento>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val commentiCollection = firestore.collection("commenti")

        val commentiList = mutableListOf<Commento>()

        commentiCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    if (document.getString("idCommento").toString() == idConsiglio) {
                        val uid = document.getString("uid").toString()
                        val dataCreazione = document.getString("data").toString()
                        val oraCreazione = document.getString("ora").toString()
                        val idCommento = document.getString("idCommento").toString()
                        val testo = document.getString("testo").toString()
                        val cappello = document.getString("cappello").toString()
                        val maglietta = document.getString("maglietta").toString()
                        val pantalone = document.getString("pantalone").toString()
                        val giacca = document.getString("giacca").toString()
                        val scarpe = document.getString("scarpe").toString()
                        val stringaAccessori = document.getString("accessori").toString()

                        val accessori = stringaAccessori.splitToSequence(",").toList()

                        val commento = Commento(
                            uid,
                            dataCreazione,
                            oraCreazione,
                            idCommento,
                            testo,
                            cappello,
                            maglietta,
                            pantalone,
                            giacca,
                            scarpe,
                            accessori
                        )
                        Log.i("COmmmento", commento.toString())
                        commentiList.add(commento)
                    }
                }

                callback(commentiList) // Chiamiamo la callback con la lista commentiList
            }

            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Error getting comments: ${exception.message}")
            }
    }

    private fun convertStringToDate(dateString: String): Date {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.parse(dateString)
    }

    private fun convertStringToTime(timeString: String): Date {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.parse(timeString)
    }
}
