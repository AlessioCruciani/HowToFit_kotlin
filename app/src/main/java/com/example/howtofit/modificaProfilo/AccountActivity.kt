package com.example.howtofit.modificaProfilo


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Placeholder
import androidx.fragment.app.Fragment
import com.example.howtofit.R
import com.example.howtofit.databinding.LayoutAccountmodBinding
import com.example.howtofit.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class AccountActivity : Fragment() {

    private lateinit var nome: String
    private lateinit var cognome : String
    private lateinit var email : String
    private lateinit var telefono: String
    private lateinit var biografia: String
    private  lateinit var username: String


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(R.layout.layout_accountmod, container, false)

        val binding: LayoutAccountmodBinding = LayoutAccountmodBinding.inflate(layoutInflater)

        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val accountNome: TextView= view.findViewById(R.id.textView)
        val editTextName: EditText = view.findViewById(R.id.editTextName)
        val editTextSurname: EditText = view.findViewById(R.id.editTextSurname)
        val textViewEmail: TextView = view.findViewById(R.id.editTextTEmail)
        val editTextTelefono: EditText = view.findViewById(R.id.editTextPhone)
        val editTextBio: EditText = view.findViewById(R.id.editTextBio)
        val immagineProfilo : ImageView = view.findViewById(R.id.imageProfilo)

        val buttonModificaDati: Button = view.findViewById(R.id.buttonModificaDati)

        val db = Firebase.firestore
        val raccoltaUtenti = db.collection("users")
        val storage = FirebaseStorage.getInstance()

        val intentToHomeActivity = Intent(requireActivity(), HomeActivity::class.java)

        //GESTISCE LA VISUALIZZAZIONE DEI DATI AL MOMENTO DELL'APERTURA DELLA PAGINA
        raccoltaUtenti.document(user?.uid.toString())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.exists()) {
                    //PRENDO LO USERNAME E LA MAIL
                    nome = documents.get("nome") as String
                    cognome = documents.get("cognome") as String
                    email = documents.get("mail") as String
                    username = documents.get("username") as String
                    if (documents.get("telefono") != null){
                        telefono = documents.get("telefono") as String
                    }else{
                    }
                    if (documents.get("telefono") != null){
                        biografia = documents.get("biografia") as String
                    }else{
                    }

                    accountNome.setText(username)
                    editTextName.setText(nome)
                    editTextSurname.setText(cognome)
                    textViewEmail.text = email
                    editTextTelefono.setText(telefono)
                    editTextBio.setText(biografia)

                    val idDocumento = documents.id
                    val storageRef = storage.reference
                    val nomeCartella = "$idDocumento/profilo/immagine_base.jpg"
                    val imageRef = storageRef.child(nomeCartella)

                    imageRef.getBytes(1024 * 1024)
                        .addOnSuccessListener { bytes ->
                            // L'immagine è stata scaricata con successo come un array di byte
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) as Bitmap
                            immagineProfilo.setImageBitmap(bitmap)
                        }
                }

            }

        buttonModificaDati.setOnClickListener{

            val nuoviDati = HashMap<String, Any>()
            nuoviDati["nome"] = editTextName.text.toString()
            nuoviDati["cognome"] = editTextSurname.text.toString()
            nuoviDati["mail"] = email
            nuoviDati["biografia"] = editTextBio.text.toString()
            nuoviDati["telefono"] = editTextTelefono.text.toString()
            nuoviDati["username"] = username


            raccoltaUtenti.document(user?.uid.toString())
                .set(nuoviDati)
                .addOnSuccessListener {
                    Log.w("DATIAGGIORNATI", "I dati sono stati aggiornati correttamente")
                    startActivity(intentToHomeActivity)
                }
                .addOnFailureListener {
                    Log.e("DATINONAGGIORNATI", "I dati non sono stati aggiornati correttamente")
                    Toast.makeText(requireActivity(), "Si è verificato un errore nella modifica, riprova", Toast.LENGTH_LONG).show()
                }


        }

        immagineProfilo.setOnClickListener{
            val fragment = ModificationImagineProfile()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.homeScroll, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

}