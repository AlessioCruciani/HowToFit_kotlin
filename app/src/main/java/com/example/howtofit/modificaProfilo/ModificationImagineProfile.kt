package com.example.howtofit.modificaProfilo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.howtofit.R
import com.example.howtofit.modificaProfilo.AccountActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class ModificationImagineProfile : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var profileImageView: ImageView
    private val storage = FirebaseStorage.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // Il codice per gestire il risultato dell'attività va qui
            onActivityResult(PICK_IMAGE_REQUEST, Activity.RESULT_OK, data)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_modifica_immagine_profilo, container, false)

        profileImageView = view.findViewById(R.id.profileImageView)
        val changeImageButton: Button = view.findViewById(R.id.changeImageButton)
        changeImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        val idDocumento = user?.uid
        val storageRef = storage.reference
        val nomeCartella = "$idDocumento/profilo/immagine_base.jpg"
        val imageRef = storageRef.child(nomeCartella)

        imageRef.getBytes(1024 * 1024)
            .addOnSuccessListener { bytes ->
                // L'immagine è stata scaricata con successo come un array di byte
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) as Bitmap
                profileImageView.setImageBitmap(bitmap)
            }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            // Carica l'immagine selezionata nell'ImageView

            val uid = user?.uid

            val storageRef = storage.reference
            val nomeCartella = uid + "/profilo/immagine_base.jpg"
            val imageRef = storageRef.child(nomeCartella)

            imageUri?.let {
                val uploadTask = imageRef.putFile(it)
                uploadTask.addOnSuccessListener { taskSnapshot ->
                    Log.i("IMMAGINE AGGIORNATA", "Immagine modificata con successo")
                    val intentToModificaProfilo = Intent(requireActivity(), AccountActivity::class.java)
                    startActivity(intentToModificaProfilo)
                }.addOnFailureListener { exception ->
                    Log.e("IMMAGINE NON AGGIORNATA", "L'immagine di profilo non è stata aggiornata")
                }
            }
        }
    }
}
