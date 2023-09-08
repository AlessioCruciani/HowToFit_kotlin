package com.example.howtofit.post

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.howtofit.R
import com.example.howtofit.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class ConsigliActivity : Fragment(),ConsiglioItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var consiglioAdapter: ConsiglioAdapter
    private var cons: MutableList<Consiglio> = mutableListOf()
    private val storage = FirebaseStorage.getInstance()
    private lateinit var popupWindow: PopupWindow
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user = auth.currentUser


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(R.layout.post_recycler_view, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPosts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Crea una lista di esempio di post
        getConsigliFromFirebase()

        // Inizializza l'adapter con la lista di post

        consiglioAdapter = ConsiglioAdapter(cons, this)
        recyclerView.adapter = consiglioAdapter
        return view
    }

    private fun convertStringToDate(dateString: String): Date {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.parse(dateString)
    }

    private fun convertStringToTime(timeString: String): Date {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.parse(timeString)
    }

    private fun getConsigliFromFirebase() {
        val firestore = FirebaseFirestore.getInstance()
        val postsCollection = firestore.collection("consigli")

        postsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                var consigliList = mutableListOf<Consiglio>()

                for (document in querySnapshot.documents) {
                    val uid = document.getString("uid").toString()
                    val desc = document.getString("desc").toString()
                    val dataEvento = document.getString("dataEvento").toString()
                    val temeEvento = document.getString("temaEvento").toString()
                    val dataCreazione = document.getString("dataCreazione").toString()
                    val oraCreazione = document.getString("oraCreazione").toString()

                    val consiglio = Consiglio(uid, desc, dataEvento, temeEvento, dataCreazione, oraCreazione, document.id)
                    consigliList.add(consiglio)

                }

                consigliList = consigliList.sortedWith(compareByDescending<Consiglio> { it.dataCreazione }.thenByDescending { it.oraCreazione })
                    .toMutableList()

                cons = consigliList
                consiglioAdapter = ConsiglioAdapter(cons, this)
                recyclerView.adapter = consiglioAdapter
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting posts: ${exception.message}")
            }
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

        if (uid == user?.uid)   {
            buttonCommenta.visibility = View.GONE
        }
        else{
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
                val linearLayoutImmaginiConsiglio = itemCommento.findViewById<LinearLayout>(R.id.linearLayoutImmaginiConsiglio)
                val cardViewImmagineConsiglio = itemCommento.findViewById<CardView>(R.id.cardViewImageCommento)
                val imageView = itemCommento.findViewById<ImageView>(R.id.imageViewCommento)
                val textViewData = itemCommento.findViewById<TextView>(R.id.textViewDataCreazioneCommento)
                val textViewOra = itemCommento.findViewById<TextView>(R.id.textViewOraCreazioneCommento)
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

                if (commento.cappello != "Non inserito"){
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
                if (commento.maglietta != "Non inserito"){
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
                if (commento.giacca != "Non inserito"){
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
                if (commento.pantalone != "Non inserito"){
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
                if (commento.scarpe != "Non inserito"){
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
                if (commento.accessori.toString() != "[]"){
                    for (accessorio in commento.accessori){
                        val imageViewAccessorio = ImageView(requireContext())
                        imageViewAccessorio.layoutParams = imageView.layoutParams
                        val cardViewImmagineAccessorio = CardView(requireContext())
                        cardViewImmagineAccessorio.layoutParams = cardViewImmagineConsiglio.layoutParams
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
        buttonCommenta.setOnClickListener{
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
                    if ( document.getString("idCommento").toString() == idConsiglio){
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

                        val commento = Commento(uid, dataCreazione, oraCreazione, idCommento, testo, cappello, maglietta, pantalone, giacca, scarpe, accessori)
                        Log.i("COmmmento", commento.toString())
                        commentiList.add(commento)
                    }
                }

                callback(commentiList) // Chiamiamo la callback con la lista commentiList
            }

            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting comments: ${exception.message}")
            }
    }
}
