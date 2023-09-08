import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.howtofit.R
import com.example.howtofit.post.Post
import com.example.howtofit.post.user
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val auth = FirebaseAuth.getInstance()
        val post = posts[position]
        val currentUserId = auth.currentUser?.uid ?: "" // Ottieni l'ID dell'utente corrente dall'autenticazione
        holder.bind(post, currentUserId)
    }

    override fun getItemCount() = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val textViewText: TextView = itemView.findViewById(R.id.textViewText)
        private val textViewAuthor: TextView = itemView.findViewById(R.id.textViewAuthor)
        private val imagePostUser: ImageView = itemView.findViewById(R.id.imagePostUser)
        private val radioGroupContainer: RadioGroup = itemView.findViewById(R.id.radioGroupChoices)
        private val storage: FirebaseStorage = FirebaseStorage.getInstance()
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
        private val button: Button = itemView.findViewById(R.id.button)
        private val button2 = itemView.findViewById<Button>(R.id.button2)

        fun bind(post: Post, currentUserId: String) {
            val postsCollection = firestore.collection("users")
            val postId = post.postId
            val postDocumentRef = firestore.collection("sondaggi").document(postId)


            postsCollection.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    if (document.id == post.uid) {
                        textViewAuthor.text = document.getString("username")
                        val storageRef: StorageReference = storage.reference
                        val nomeCartella = "${document.id}/profilo/immagine_base.jpg"
                        val imageRef: StorageReference = storageRef.child(nomeCartella)

                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(itemView)
                                .load(uri)
                                .into(imagePostUser)
                        }.addOnFailureListener { exception ->
                            Log.e(TAG, "Error downloading image: ${exception.message}")
                        }
                    }
                }
            }

            textViewText.text = post.desc
            radioGroupContainer.setOnCheckedChangeListener(null)
            radioGroupContainer.removeAllViews()

            for ((index, scelta) in post.scelte.withIndex()) {
                val optionLayout = LayoutInflater.from(itemView.context).inflate(R.layout.option_layout, radioGroupContainer, false)
                val imageView = optionLayout.findViewById<ImageView>(R.id.imageViewOption)
                val radioButton = optionLayout.findViewById<RadioButton>(R.id.radioButtonOption)

                if (scelta is Bitmap) {
                    imageView.setImageBitmap(scelta)
                }

                radioButton.isChecked = index == post.selectedIndex
                radioButton.setOnClickListener {
                    post.selectedIndex = index
                    notifyDataSetChanged()
                }

                radioGroupContainer.addView(optionLayout)
            }

                postDocumentRef.collection("votes").whereEqualTo("userId", currentUserId).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty && post.uid==currentUserId) {
                        button.visibility = View.GONE
                        button2.visibility = View.VISIBLE
                    } else {
                        button.visibility = View.VISIBLE
                        button2.visibility = View.GONE
                    }
                }
            button2.setOnClickListener {
                val barPercentage1 = itemView.findViewById<View>(R.id.barOption1)
                val barPercentage2 = itemView.findViewById<View>(R.id.barOption2)
                val radioGroup = itemView.findViewById<RadioGroup>(R.id.radioGroupChoices)
                val horizontal = itemView.findViewById<LinearLayout>(R.id.horizontalLayout)
                val percTot = itemView.findViewById<TextView>(R.id.percentualTot)
                val perc1 = itemView.findViewById<TextView>(R.id.percentual1)
                val perc2 = itemView.findViewById<TextView>(R.id.percentual2)

                radioGroup.visibility = View.GONE
                button2.visibility = View.GONE
                horizontal.visibility = View.VISIBLE

                postDocumentRef.collection("votes").get()
                    .addOnSuccessListener { querySnapshot ->
                        // Conta il numero di voti per ciascuna opzione
                        val numOption = post.scelte.size
                        val votesCount = Array(numOption) { 0 }
                        for (document in querySnapshot.documents) {
                            val selectedOption = document.getLong("selectedOption")?.toInt() ?: -1
                            if (selectedOption in 0 until votesCount.size) {
                                votesCount[selectedOption]++
                            }
                        }

                        // Calcola le altezze proporzionali delle barre
                        val totalVotes = votesCount.sum()
                        val percentageOption1 = if (totalVotes > 0) (votesCount[0] / totalVotes.toFloat()) else 0f
                        val percentageOption2 = if (totalVotes > 0) (votesCount[1] / totalVotes.toFloat()) else 0f
                        //Aggiorna
                        val paramsBar1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 40)
                        val paramsBar2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 40)

                        val maxWidth = horizontal.width
                        val barWidth1 = (maxWidth * percentageOption1).toInt()
                        val barWidth2 = (maxWidth * percentageOption2).toInt()

                        paramsBar1.weight = percentageOption1
                        paramsBar1.width = barWidth1
                        paramsBar2.width = barWidth2
                        paramsBar2.weight = percentageOption2

                        barPercentage1.layoutParams = paramsBar1
                        barPercentage2.layoutParams = paramsBar2

                        percTot.text = totalVotes.toString()
                        perc1.text = (percentageOption1 * 100).toString() + "%"
                        perc2.text = (percentageOption2 * 100).toString() + "%"

                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error retriving vote", e)
                    }

            }

            button.setOnClickListener {
                val selectedOption = post.selectedIndex
                val barPercentage1 = itemView.findViewById<View>(R.id.barOption1)
                val barPercentage2 = itemView.findViewById<View>(R.id.barOption2)
                val radioGroup = itemView.findViewById<RadioGroup>(R.id.radioGroupChoices)
                val horizontal = itemView.findViewById<LinearLayout>(R.id.horizontalLayout)
                val percTot = itemView.findViewById<TextView>(R.id.percentualTot)
                val perc1 = itemView.findViewById<TextView>(R.id.percentual1)
                val perc2 = itemView.findViewById<TextView>(R.id.percentual2)

                if (selectedOption == -1) {
                    // Mostra un Toast per avvisare l'utente di scegliere un'opzione
                    Toast.makeText(itemView.context, "Seleziona un'opzione prima di votare", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val voteData = hashMapOf(
                    "userId" to currentUserId,
                    "selectedOption" to selectedOption
                )

                radioGroup.visibility = View.GONE
                button.visibility = View.GONE
                horizontal.visibility = View.VISIBLE

                postDocumentRef.collection("votes").add(voteData)
                    .addOnSuccessListener { documentReference ->
                        Log.d(TAG, "Vote saved with ID: ${documentReference.id}")
                        // Qui puoi effettuare eventuali azioni dopo il salvataggio del voto
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error saving vote", e)
                    }

                postDocumentRef.collection("votes").get()
                    .addOnSuccessListener { querySnapshot ->
                        // Conta il numero di voti per ciascuna opzione
                        val numOption = post.scelte.size
                        val votesCount = Array(numOption) { 0 }
                        for (document in querySnapshot.documents) {
                            val selectedOption = document.getLong("selectedOption")?.toInt() ?: -1
                            if (selectedOption in 0 until votesCount.size) {
                                votesCount[selectedOption]++
                            }
                        }

                        // Calcola le altezze proporzionali delle barre
                        val totalVotes = votesCount.sum()
                        val percentageOption1 = if (totalVotes > 0) (votesCount[0] / totalVotes.toFloat()) else 0f
                        val percentageOption2 = if (totalVotes > 0) (votesCount[1] / totalVotes.toFloat()) else 0f
                        //Aggiorna
                        val paramsBar1 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 40)
                        val paramsBar2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 40)

                        val maxWidth = horizontal.width
                        val barWidth1 = (maxWidth * percentageOption1).toInt()
                        val barWidth2 = (maxWidth * percentageOption2).toInt()

                        paramsBar1.weight = percentageOption1
                        paramsBar1.width = barWidth1
                        paramsBar2.width = barWidth2
                        paramsBar2.weight = percentageOption2

                        barPercentage1.layoutParams = paramsBar1
                        barPercentage2.layoutParams = paramsBar2

                        percTot.text = totalVotes.toString()
                        perc1.text = (percentageOption1 * 100).toString() + "%"
                        perc2.text = (percentageOption2 * 100).toString() + "%"

                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error saving vote", e)
                    }

                Log.d(TAG, "Post:${post.postId}")
            }
        }
    }

    companion object {
        private const val TAG = "PostAdapter"
    }
}
