package com.example.howtofit.post

import PostAdapter
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.howtofit.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class PostActivity : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var posts: MutableList<Post> = mutableListOf()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(R.layout.post_recycler_view, container, false)

        // Crea una lista di esempio di post
        getPostsFromFirebase()

        recyclerView = view.findViewById(R.id.recyclerViewPosts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Inizializza l'adapter con la lista di post

        postAdapter = PostAdapter(posts)
        recyclerView.adapter = postAdapter
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

    private fun getPostsFromFirebase() {
        val firestore = FirebaseFirestore.getInstance()
        val postsCollection = firestore.collection("sondaggi")

        postsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val postsList = mutableListOf<Post>()

                for (document in querySnapshot.documents) {
                    val uid = document.getString("uid").toString()
                    val desc = document.getString("desc").toString()
                    val date = document.getString("date").toString()
                    val postId = document.getString("postId").toString()
                    val time = document.getString("time").toString()
                    val scelte = document.get("img").toString()

                    val data = convertStringToDate(date)
                    val ora = convertStringToTime(time)

                    var lista: List<String> = emptyList()
                    lista = scelte.split(",") // Dividi la stringa utilizzando la virgola come delimitatore
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
                                    Log.e(TAG, "Error downloading image: ${e.message}")
                                }
                            }

                            downloadTask.await()
                        }
                    }

                    val post = Post(uid, desc, data,postId, ora, bitmapList)
                    postsList.add(post)
                }

                postsList.sortByDescending { it.date }
                posts = postsList
                postAdapter = PostAdapter(posts)
                recyclerView.adapter = postAdapter
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting posts: ${exception.message}")
            }
    }

}