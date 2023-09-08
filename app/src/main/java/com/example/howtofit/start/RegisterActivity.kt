package com.example.howtofit.start

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.howtofit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class RegisterActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val campoPassword: EditText = findViewById(R.id.editPsw)
        val campoRepeatPassword: EditText = findViewById(R.id.editRepeatPsw)
        val campoEmail: EditText = findViewById(R.id.editEmailAddress)
        val campoNome: EditText = findViewById(R.id.editNome)
        val campoCognome: EditText = findViewById(R.id.editCognome)
        val campoUsername: EditText = findViewById(R.id.editUsername)
        val occhiolinoPassword: ImageView = findViewById(R.id.imageViewMostraNascondiPsw)
        val occhiolinoRepeatPassword: ImageView = findViewById(R.id.imageViewMostraNascondiRipetiPsw)
        val buttonRegister: Button = findViewById(R.id.buttonRegistrati)
        val tornaAlLogin: TextView = findViewById(R.id.textViewTornaAlLogin)
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val raccoltaUsers = db.collection("users")
        val storage = FirebaseStorage.getInstance()

        tornaAlLogin.setOnClickListener(){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        occhiolinoPassword.setOnClickListener(){
            if (campoPassword.inputType.toString() == "225"){
                campoPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            }else if(campoPassword.inputType.toString() == "224" || campoPassword.inputType.toString() == "144"){
                campoPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
            }
        }

        occhiolinoRepeatPassword.setOnClickListener(){
            if (campoRepeatPassword.inputType.toString() == "225"){
                campoRepeatPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            }else if(campoRepeatPassword.inputType.toString() == "224" || campoRepeatPassword.inputType.toString() == "144"){
                campoRepeatPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
            }
        }

        fun controllaSeVuoto(): Boolean {
            if (campoPassword.text.toString().isEmpty()
                or campoEmail.text.toString().isEmpty()
                or campoRepeatPassword.text.toString().isEmpty()
                or campoNome.text.toString().isEmpty()
                or campoCognome.text.toString().isEmpty()
                or campoUsername.text.toString().isEmpty()
                ){
                Toast.makeText(
                    baseContext,
                    "Devi ancora inserire dei dati",
                    Toast.LENGTH_LONG,
                ).show()
                return false
            }
            else{
                return true
            }
        }

        buttonRegister.setOnClickListener(){ view ->
            //CONTROLLA CHE LE DUE PASSWORD INSERITE SIANO UGUALI
            if ((campoPassword.text.toString() == campoRepeatPassword.text.toString())and controllaSeVuoto()){

                val nome = campoNome.text.toString()
                val cognome = campoCognome.text.toString()
                val username = campoUsername.text.toString()
                val email = campoEmail.text.toString()
                val password = campoPassword.text.toString()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser

                            Toast.makeText(
                                baseContext,
                                "Account Created",
                                Toast.LENGTH_LONG,
                            ).show()

                            //QUI DEVO PRENDERE LO UID DELL'UTENTE CREATO E SALVARE I DATI MANCANTI NEL DATABASE
                            val documentId = user?.uid

                            val userFields = hashMapOf(
                                "nome" to nome,
                                "cognome" to cognome,
                                "mail" to email,
                                "username" to username
                            )

                            raccoltaUsers.document(documentId.toString())
                                .set(userFields)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        baseContext,
                                        "aggiunto",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        baseContext,
                                        "problemi",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            //CREA LA CARTELLA DELL'UTENTE CON LE IMMAGINI
                            val storageRef = storage.reference

                            val nomeCartella = documentId.toString() + "/profilo/immagine_base.jpg"

                            val imageRef = storageRef.child(nomeCartella)

                            val drawableId = R.drawable.user_base_image
                            val bitmap = BitmapFactory.decodeResource(resources, drawableId)
                            val baos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val data = baos.toByteArray()

                            val uploadTask = imageRef.putBytes(data)
                            uploadTask.addOnSuccessListener {
                                Toast.makeText(
                                    baseContext,
                                    "immagine caricata con successo",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.addOnFailureListener { exception ->
                                Toast.makeText(
                                    baseContext,
                                    "immagine non caricata",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
            }
            else{
                Toast.makeText(
                    baseContext,
                    "Le due password devono essere uguali",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }
}