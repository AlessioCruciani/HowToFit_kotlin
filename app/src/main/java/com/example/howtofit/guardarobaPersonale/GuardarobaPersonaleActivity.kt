package com.example.howtofit.guardarobaPersonale

import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.howtofit.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class GuardarobaPersonaleActivity : Fragment() {

    private lateinit var galleryRecyclerView: RecyclerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var guardarobaAdapter: GuardarobaAdapter
    private lateinit var fabAddImage: FloatingActionButton
    private lateinit var immagineGuardaroba: ImageView
    private lateinit var popupWindow: PopupWindow
    private val abbigliamento = listOf("cappelli", "magliette", "giacche", "pantaloni", "scarpe", "accessori")
    private lateinit var imageButton: ImageButton
    private lateinit var deleteButton: Button
    private lateinit var imageUrl: Uri
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.guardaroba_personale, container, false)

        // Inizializza la RecyclerView
        galleryRecyclerView = view.findViewById(R.id.gallery_recycler_view)
        deleteButton = view.findViewById(R.id.deleteButton)

        val screenSize = getScreenSize(requireContext())
        val screenWidth = screenSize.first

        val dpValue = 150 // Valore in dp
        val scale = resources.displayMetrics.density
        val pixelValue = (dpValue * scale + 0.5f).toInt()

        galleryRecyclerView.layoutManager = GridLayoutManager(requireContext(), screenWidth / pixelValue)

        guardarobaAdapter = GuardarobaAdapter(getImageList(), deleteButton)
        galleryRecyclerView.adapter = guardarobaAdapter

        // Inizializza il FloatingActionButton
        fabAddImage = view.findViewById(R.id.fab)

        fabAddImage.setOnClickListener {
            openPopupWindow()
        }

        return view
    }

    fun openPopupWindow() {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_guardaroba_personale, null)

        // Imposta le dimensioni desiderate per il popup
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT

        // Crea la finestra di popup
        popupWindow = PopupWindow(popupView, width, height, true)
        popupWindow.animationStyle = R.style.PopupAnimation

        val closeButton: ImageView = popupView.findViewById(R.id.closeButton)
        val spinner: Spinner = popupView.findViewById(R.id.spinner_abbigliamento)
        imageButton = popupView.findViewById(R.id.buttonAddImageGP)
        val button: Button = popupView.findViewById(R.id.buttonInserisciImmagineGP)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, abbigliamento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        closeButton.setOnClickListener {
            popupWindow.dismiss()
        }

        imageButton.setOnClickListener {
            // Avvia l'intent per selezionare un'immagine dalla galleria
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_SELECT)
        }

        button.setOnClickListener {
            val selectedCategory = spinner.selectedItem.toString()
            Log.e("uid", user?.uid.toString())
            val randomString = generateRandomString(15)
            val storageRefWherePutImages = storageRef.child("${user?.uid}/guardaroba/$selectedCategory/image$randomString.jpg")
            val uploadTask: UploadTask = storageRefWherePutImages.putFile(imageUrl)
            uploadTask.addOnSuccessListener {
                Toast.makeText(requireContext(), "Immagine aggiunta correttamente a $selectedCategory", Toast.LENGTH_LONG).show()

            }
                .addOnFailureListener {
                    Log.e("ErroreNelCaricamento", "Errore nel caricamento dell'immagine")
                }
                .addOnCompleteListener {
                    guardarobaAdapter = GuardarobaAdapter(getImageList(), deleteButton)
                    galleryRecyclerView.adapter = guardarobaAdapter
                    popupWindow.dismiss()
                }
        }
        popupWindow.showAtLocation(fabAddImage, Gravity.CENTER, 0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data != null) {
            imageUrl = data.data!!

            Glide.with(this)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageButton)

            Toast.makeText(requireContext(), "Click the image to change again", Toast.LENGTH_LONG).show()
        }
    }

    private fun getImageList(): List<String> {

        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val uid = user?.uid.toString()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageUrls = mutableListOf<String>()

        for (i in abbigliamento) {
            val folderRef = storageRef.child("$uid/guardaroba/$i")
            folderRef.listAll()
                .addOnSuccessListener { listResult ->
                    for (item in listResult.items) {
                        item.downloadUrl
                            .addOnSuccessListener { uri ->
                                val imageUrl = uri.toString()
                                imageUrls.add(imageUrl)
                                guardarobaAdapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Immagini non caricate", "l'url dell'immagine non è stato inserito nella lista")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Immagini non ottenute", "Gli url delle immagini non sono stati ottenuti")
                }
        }
        return imageUrls
    }

    @Suppress("DEPRECATION")
    fun getScreenSize(context: Context): Pair<Int, Int> {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val screenWidth = windowMetrics.bounds.width() - insets.left - insets.right
            val screenHeight = windowMetrics.bounds.height() - insets.top - insets.bottom
            Pair(screenWidth, screenHeight)
        } else {
            val display = windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }

    fun generateRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    companion object {
        private const val REQUEST_IMAGE_SELECT = 1001
    }
}
