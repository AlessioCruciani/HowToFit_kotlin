package com.example.howtofit.home

import UserListAdapter
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.howtofit.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment

class UserListActivity : Fragment(), ValueEventListener, UserListAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserListAdapter
    private var filter: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(R.layout.activity_user_list, container, false)

        recyclerView = view.findViewById(R.id.user_list_recycler_view)
        adapter = UserListAdapter(emptyList(),parentFragmentManager)
        adapter.setOnItemClickListener(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val db = Firebase.firestore
        val storage = FirebaseStorage.getInstance()
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val userList = mutableListOf<User>()

                for (document in documents) {

                    var user = document.toObject(User::class.java)
                    val storageRef = storage.reference
                    val cartella = "${document.id}/profilo/immagine_base.jpg"
                    val immagineRef = storageRef.child(cartella)

                    immagineRef.getBytes(1024 * 1024)
                        .addOnSuccessListener { bytes ->
                            // L'immagine è stata scaricata con successo come un array di byte
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) as Bitmap
                            user.profileImage = bitmap

                            adapter.notifyDataSetChanged()
                        }
                        .addOnCompleteListener{

                        }

                    userList.add(user)
                }

                adapter = UserListAdapter(userList,parentFragmentManager)
                adapter.setOnItemClickListener(this)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Errore durante il recupero degli utenti: ", exception)
            }
        val searchView = view.findViewById<SearchView>(R.id.search_plate)
        searchView.queryHint = "Cerca utenti"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
        return view
    }


    companion object {
        private const val TAG = "UserListActivity"
    }

    override fun onDataChange(snapshot: DataSnapshot) {

    }

    override fun onCancelled(error: DatabaseError) {
        Log.e("UserListActivity", "Database error: ${error.message}")
    }

    override fun onItemClick(user: User) {
        val toastMessage = "${user.username}"
        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()
        val fragment = UserAccountFragment()

        // Passa l'ID dell'utente selezionato come argomento al nuovo fragment
        val bundle = Bundle()
        bundle.putString("username", user.username)
        fragment.arguments = bundle

        // Sostituisci il fragment corrente con il nuovo fragment degli account dell'utente
        parentFragmentManager.beginTransaction()
            .replace(R.id.homeScroll, fragment)
            .addToBackStack(null)
            .commit()

    }
}
