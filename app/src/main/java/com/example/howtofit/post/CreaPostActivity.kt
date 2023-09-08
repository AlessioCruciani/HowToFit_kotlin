package com.example.howtofit.post

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Radio
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.howtofit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class CreaPostActivity : AppCompatActivity() {

    private var selectedImageBitmap: Bitmap? = null
    private lateinit var rbs: RadioButton
    private lateinit var rbc: RadioButton
    private lateinit var linearSondaggio: LinearLayout
    private lateinit var linearConsiglio: LinearLayout
    private lateinit var editTextDataEvento: EditText
    private val calendar = Calendar.getInstance()
    private lateinit var recyclerViewFoto: RecyclerView
    private val fotoList: MutableList<Bitmap?> = mutableListOf()
    private lateinit var fotoAdapter: FotoAdapter
    private val REQUEST_GALLERY = 1
    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextDescrizionePost: EditText
    private lateinit var editTextTemaEvento: EditText
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user = auth.currentUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crea_post)

        val close = findViewById<ImageButton>(R.id.close)
        rbs = findViewById(R.id.radioButtonSondaggio)
        rbc = findViewById(R.id.radioButtonConsiglio)
        linearConsiglio = findViewById(R.id.linearConsiglio)
        linearSondaggio = findViewById(R.id.linearSondaggio)
        editTextDataEvento = findViewById(R.id.editTextDataEvento)
        firestore = FirebaseFirestore.getInstance()
        editTextDescrizionePost = findViewById(R.id.editTextDescrizionePost)
        editTextTemaEvento = findViewById(R.id.editTextTemaEvento)


close.setOnClickListener {
finish()
}

        rbc.setOnClickListener() {
            onRadioButtonClicked(rbc)
        }
        rbs.setOnClickListener() {
            onRadioButtonClicked(rbs)
        }

        recyclerViewFoto = findViewById(R.id.recyclerViewFoto)

        // Configura il RecyclerView
        recyclerViewFoto.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        fotoAdapter = FotoAdapter(fotoList)
        recyclerViewFoto.adapter = fotoAdapter

        fotoAdapter.setOnItemClickListener(object : FotoAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // Rimuovi l'immagine dalla lista
                fotoList.removeAt(position)
                // Notifica l'adattatore che un elemento è stato rimosso
                fotoAdapter.notifyItemRemoved(position)
            }
        })
    }

    fun aggiungiFoto(view: View) {
        // Apri l'app della galleria per selezionare un'immagine
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                selectedImageBitmap = getBitmapFromUri(imageUri)
                fotoList.add(selectedImageBitmap) // Aggiungi la Bitmap alla lista
                fotoAdapter.notifyDataSetChanged()
                recyclerViewFoto.scrollToPosition(fotoList.size - 1)
            }
        }
    }

    fun creaConsiglio(view: View) {
        val uid = user?.uid.toString()
        val descrizione = editTextDescrizionePost.text.toString().trim()
        val dataEvento = editTextDataEvento.text.toString().trim()
        val temaEvento = editTextTemaEvento.text.toString().trim()
        val dataCreazione = getCurrentDate()
        val oraCreazione = getCurrentTime()

        val consiglioData = hashMapOf(
            "uid" to uid,
            "desc" to descrizione,
            "dataEvento" to dataEvento,
            "temaEvento" to temaEvento,
            "dataCreazione" to dataCreazione,
            "oraCreazione" to oraCreazione
        )

        firestore.collection("consigli")
            .add(consiglioData)
        finish()
    }


    fun creaSondaggio(view: View) {
        // Recupera i dati dal layout
        val uid = user?.uid.toString()// Sostituisci con il vero UID dell'utente
        val descrizione = editTextDescrizionePost.text.toString().trim()
        val dataCreazione = getCurrentDate()
        val oraCreazione = getCurrentTime()

        // Crea un nuovo documento nel database
        val sondaggioData = hashMapOf(
            "uid" to uid,
            "desc" to descrizione,
            "date" to dataCreazione,
            "time" to oraCreazione
        )

        firestore.collection("sondaggi")
            .add(sondaggioData)
            .addOnSuccessListener { documentReference ->
                val sondaggioId = documentReference.id // Ottieni l'ID del documento appena creato

                // Carica le immagini nello storage di Firebase
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child("sondaggi/$sondaggioId")

                for ((index, bitmap) in fotoList.withIndex()) {
                    val fileName = "image_$index.jpg"
                    val imageRef = storageRef.child(fileName)
                    val baos = ByteArrayOutputStream()
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()

                    // Carica l'immagine nello storage
                    val uploadTask = imageRef.putBytes(data)
                    uploadTask.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // L'immagine è stata caricata con successo
                            // Aggiungi il nome dell'immagine al campo "img" del documento del sondaggio
                            sondaggioData["img"] = sondaggioData["img"].toString() + "," + fileName
                            // Aggiorna i dati del sondaggio nel database
                            documentReference.update("img", sondaggioData["img"])
                                .addOnSuccessListener {
                                    // Aggiornamento completato con successo
                                    // Puoi fare altre operazioni qui se necessario
                                }
                                .addOnFailureListener { exception ->
                                    // Gestisci l'errore nell'aggiornamento del campo "img" nel documento del sondaggio
                                    // Puoi mostrare un messaggio di errore o registrare l'errore nel log
                                }
                        } else {
                            // Gestisci l'errore nel caricamento dell'immagine nello storage
                            // Puoi mostrare un messaggio di errore o registrare l'errore nel log
                        }
                    }
                }

                // Aggiungi un listener per le immagini caricate nello storage
                // Puoi eseguire un'operazione dopo che tutte le immagini sono state caricate
                // Ad esempio, puoi avviare un'altra attività o mostrare un messaggio di successo.
                // Tieni presente che il caricamento delle immagini può richiedere del tempo, quindi questo listener verrà chiamato in un momento successivo.
                storageRef.listAll().addOnSuccessListener { listResult ->
                    val imagesUploaded = listResult.items.size
                    if (imagesUploaded == fotoList.size) {
                        // Tutte le immagini sono state caricate correttamente
                        // Puoi fare altre operazioni qui se necessario
                        // Ad esempio, puoi avviare un'altra attività o mostrare un messaggio di successo.
                    } else {
                        // Non tutte le immagini sono state caricate correttamente
                        // Puoi gestire l'errore in base alle tue esigenze
                    }
                }
                finish()
            }
            .addOnFailureListener { exception ->
                // Gestisci l'errore nella creazione del documento del sondaggio nel database
                // Puoi mostrare un messaggio di errore o registrare l'errore nel log
            }
    }

    // Funzione di utilità per ottenere la data corrente nel formato desiderato
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



    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radioButtonSondaggio ->
                    if (checked) {
                        rbc.isChecked = false
                        linearConsiglio.visibility = View.GONE
                        linearSondaggio.visibility = View.VISIBLE
                    }
                R.id.radioButtonConsiglio ->
                    if (checked) {
                        rbs.isChecked = false
                        linearConsiglio.visibility = View.VISIBLE
                        linearSondaggio.visibility = View.GONE
                    }
            }
        }
    }

    fun showDatePickerDialog(view: View) {
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                // Aggiorna il campo di testo con la data selezionata
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateString = dateFormat.format(selectedDate.time)
                editTextDataEvento.setText(dateString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Imposta una data minima (opzionale)
        // val minDateCalendar = Calendar.getInstance()
        // minDateCalendar.set(2022, Calendar.JANUARY, 1)
        // datePickerDialog.datePicker.minDate = minDateCalendar.timeInMillis

        // Mostra il dialogo del picker della data
        datePickerDialog.show()
    }

}

