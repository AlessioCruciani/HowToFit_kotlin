package com.example.howtofit.home

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.howtofit.R
import com.example.howtofit.databinding.ActivityHomeBinding
import com.example.howtofit.guardarobaPersonale.GuardarobaPersonaleActivity
import com.example.howtofit.modificaProfilo.AccountActivity
import com.example.howtofit.post.ConsigliActivity
import com.example.howtofit.post.PostActivity
import com.example.howtofit.start.LoginActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class HomeActivity : AppCompatActivity() {

    // questa variabile rappresenta le tre linee che aprono il navigation drawer
    lateinit var toogle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = findViewById(R.id.drawerLayout)

        val db = Firebase.firestore
        val raccoltaUtenti = db.collection("users")
        val storage = FirebaseStorage.getInstance()
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navHeader: View = navView.getHeaderView(0)
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val usernameTextView: TextView = navHeader.findViewById(R.id.username)
        val emailTextView: TextView = navHeader.findViewById(R.id.email_drawer)
        val immagineProfilo: ImageView = navHeader.findViewById(R.id.immagineProfilo)
        val consigliButton: ImageButton = findViewById(R.id.consigliButton)
        val sondaggiButton: ImageButton = findViewById(R.id.sondaggioButton)

        raccoltaUtenti.document(user?.uid.toString())
            .get()
            .addOnSuccessListener { documents ->
                if (documents.exists()) {
                    //PRENDO LO USERNAME E LA MAIL
                    val usernameLoggato = documents.get("username") as String
                    val emailLoggato = documents.get("mail") as String

                    usernameTextView.text = usernameLoggato
                    emailTextView.text = emailLoggato

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
        val fragmentContainer = findViewById<FrameLayout>(R.id.homeScroll)

        // Controlla se il fragment predefinito è già presente nel FrameLayout
        if (supportFragmentManager.findFragmentById(R.id.homeScroll) == null) {
            // Il fragment predefinito non è presente, quindi aggiungilo
            val defaultFragment = PostActivity()
            supportFragmentManager.beginTransaction()
                .replace(R.id.homeScroll, defaultFragment)
                .commit()
            Log.d("HomeActivity", "Fragment predefinito caricato con successo")
        }

        //per far funzionare il drawer
        toogle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toogle)
        toogle.syncState()


        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_mod -> {
                    val fragment = AccountActivity()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.homeScroll, fragment)
                        .commit() }
                R.id.nav_logout -> {
                    val auth: FirebaseAuth = FirebaseAuth.getInstance()
                    val intent = Intent(this,LoginActivity::class.java)
                    auth.signOut()
                    startActivity(intent)
                    finish()
                }
                R.id.nav_personalWardrobe -> {
                    consigliButton.visibility = View.GONE
                    val fragment = GuardarobaPersonaleActivity()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.homeScroll, fragment)
                        .commit()
                }
            }
            true
        }

        //lavoriamo sugli elementi del footer

        binding.consigliButton.setOnClickListener {
            replaceFragment(ConsigliActivity())
            consigliButton.visibility = View.GONE
            sondaggiButton.visibility = View.VISIBLE
        }

        binding.sondaggioButton.setOnClickListener {
            replaceFragment(PostActivity())
            consigliButton.visibility = View.VISIBLE
            sondaggiButton.visibility = View.GONE
        }

        binding.home.setOnClickListener{
            replaceFragment(PostActivity())
            consigliButton.visibility = View.VISIBLE
        }

        binding.wardrobe.setOnClickListener(){
            replaceFragment(ProfiloActivity())
            sondaggiButton.visibility = View.GONE
            consigliButton.visibility = View.GONE
        }

        binding.search.setOnClickListener(){
            replaceFragment(UserListActivity())
            sondaggiButton.visibility = View.GONE
            consigliButton.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toogle.onOptionsItemSelected(item)){

            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.homeScroll,fragment)
        fragmentTransaction.commit()

    }
}