package com.example.howtofit.start

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.howtofit.R
import com.example.howtofit.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class LoginActivity : AppCompatActivity() {

    private var authGoogle : FirebaseAuth? = null
    private lateinit var googleSignInClient : GoogleSignInClient
    private val db = Firebase.firestore
    private val raccoltaUsers = db.collection("users")
    private val storage = FirebaseStorage.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editTextEmail: EditText = findViewById(R.id.editEmailLogin)
        val editTextPsw: EditText = findViewById(R.id.editPassword)
        val buttonLogin: Button = findViewById(R.id.buttonSubmit)
        val buttonLoginGoogle: Button = findViewById(R.id.signGoogle)
        val occhiolinoLogin: ImageView = findViewById(R.id.imageViewMostraNascondiPswLogin)
        val buttonToRegister: Button = findViewById(R.id.creaAccount)
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val intentToHomePage = Intent(this, HomeActivity::class.java)
        val intentToChangePsw = Intent(this, RecuperaPassword::class.java)
        val buttonToPswChange: TextView = findViewById(R.id.textViewPswDimenticata)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.your_web_client_id))
            .requestEmail()
            .build()
        this.googleSignInClient = GoogleSignIn.getClient(this , gso)
        authGoogle = FirebaseAuth.getInstance()

        //GESTISCE IL LOGIN CON GOOGLE
        buttonLoginGoogle.setOnClickListener(){
            signInGoogle()
        }
        //GESTISCE LA VISIBILITA DELLA PASSWORD NEL LOGIN
        occhiolinoLogin.setOnClickListener(){
            if (editTextPsw.inputType.toString() == "225"){
                editTextPsw.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            }else if(editTextPsw.inputType.toString() == "224" || editTextPsw.inputType.toString() == "144"){
                editTextPsw.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
            }
        }
        //GESTISCE IL CAMBIO DI ACTIVITY VERSO LA REGISTRAZIONE
        buttonToRegister.setOnClickListener(){
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        //GESTISCE IL CAMBIO DI ACTIVITY VERSO IL CAMBIO PASSWORD
        buttonToPswChange.setOnClickListener(){
            startActivity(intentToChangePsw)
            finish()
        }
        //EFFETTUA UN CONTROLLO SUI CAMPI -- RISPONDE FALSE SE ANCHE UNO SOLO E' VUOTO
        fun controllaSeVuoto(): Boolean {
            if (editTextPsw.text.toString().isEmpty()
                or editTextEmail.text.toString().isEmpty()){
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
        //GESTISCE IL LOGIN CON LE CREDENZIALI DELL'ACCOUNT CREATO TRAMITE FIREBASE
        buttonLogin.setOnClickListener() {
            val email = editTextEmail.text.toString()
            val password = editTextPsw.text.toString()
            if (controllaSeVuoto()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val user = auth.currentUser
                            startActivity(intentToHomePage)
                            finish()

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                baseContext,
                                "Authentication failed.",
                                Toast.LENGTH_SHORT,
                            ).show()

                        }
                    }

            }
        }
    }
    //RICHIEDE L'APERTURA DELLA PAGINA PER IL LOGIN CON GOOGLE
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    //AVVIA IL LAUNCHER PER IL LOGIN E LA SCELTA DEGLI ACCOUNT
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }
    //SE LA TASK VA A BUON FINE PRENDE I DATI DELL'UTENTE LOGGATO
    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                Toast.makeText(this, "Benvenuto "+account.givenName.toString(), Toast.LENGTH_LONG).show()
                updateUI(account)
            }
        }else{
            Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }
    //AGGIORNA L'INTERFACCIA E GESTISCE L'APERTURA DELLA HOME PAGE
    //ESEGUE UN CONTROLLO SUL DATABASE PER SAPERE SE E' IL PRIMO LOGIN PER L'UTENTE
    //SE E' IL PRIMO LOGIN CREA UN DOCUMENTO NELLA RACCOLTA USER CON I DATI DELL'UTENTE
    //UNA VOLTA CREATO IL DOCUMENTO CREA UNA CARTELLA NELLO STORAGE CON L'IMMAGINE DI PROFILO DELL'UTENTE
    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        authGoogle?.signInWithCredential(credential)?.addOnCompleteListener {
            if (it.isSuccessful){

                val currentUser = authGoogle?.currentUser
                val intentToHomePage = Intent(this, HomeActivity::class.java)
                startActivity(intentToHomePage)


                // Verifica se l'utente è loggato con Google
                if (currentUser != null && currentUser.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }) {
                    // L'utente è loggato con Google, ottieni il suo UID
                    val uid = currentUser.uid
                    //CONTROLLO CHE NEL DATABASE SIA PRESENTE UNA RACCOLTA PER L'UTENTE LOGGATO
                    raccoltaUsers.document(uid)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.exists()) {
                                //GENERO UN NUMERO CASUALE DA ASSOCIARE AL NOME E AL COGNOME
                                val randomNumber = (Math.random() * 999).toInt() + 1
                                //INSERISCO I VALORI NELL'HASHMAP
                                val userFields = hashMapOf(
                                    "nome" to account.givenName.toString(),
                                    "cognome" to account.familyName.toString(),
                                    "mail" to account.email.toString(),
                                    "username" to account.givenName.toString()+account.familyName.toString()+randomNumber.toString()
                                )
                                //CREO UNA RACCOLTA CON LO UID DELL'UTENTE
                                raccoltaUsers.document(uid)
                                    .set(userFields)
                                    .addOnSuccessListener {
                                        Log.i("LoginGoogleCreated", "Cartella creata")
                                    }
                                //CREO UNA CARTELLA NELLO STORAGE CON LE IMMAGINI DELL'UTENTE
                                val storageRef = storage.reference
                                val nomeCartella = uid + "/profilo/immagine_base.jpg"
                                val imageRef = storageRef.child(nomeCartella)

                                Glide.with(this)
                                    .asBitmap()
                                    .load(account.photoUrl)
                                    .into(object : CustomTarget<Bitmap>() {
                                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                            val baos = ByteArrayOutputStream()
                                            resource.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                                            val data = baos.toByteArray()
                                            val uploadTask = imageRef.putBytes(data)
                                            uploadTask.addOnSuccessListener {
                                                Log.d("ImmagineCaricata", "Image uploaded successfully")
                                            }.addOnFailureListener { e ->
                                                Log.e("Immagine Non Caricata", "Error uploading image: ${e.message}")
                                            }
                                        }

                                        override fun onLoadCleared(placeholder: Drawable?) {
                                            // Do nothing
                                        }
                                    })
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.i("LoginGoogleAlreadyExists", "La cartella esiste già")
                        }
                }
            }
        }
        authGoogle?.signInWithCredential(credential)?.addOnFailureListener{
            Toast.makeText(this, "c'è stato un errore", Toast.LENGTH_LONG).show()
        }
    }
}