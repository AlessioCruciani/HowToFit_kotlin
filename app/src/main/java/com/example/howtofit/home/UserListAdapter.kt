import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.howtofit.R
import com.example.howtofit.home.User
import java.util.*

class UserListAdapter(
    private val userList: List<User>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<UserListAdapter.UserViewHolder>(), Filterable {

    private var itemClickListener: OnItemClickListener? = null
    private var userListFiltered: List<User> = userList
    private var userListAll: List<User> = userList

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.username_text_view)
        val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        val profileImageView: ImageView = itemView.findViewById(R.id.profile_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userListFiltered[position]

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(user)
        }

        holder.usernameTextView.text = user.username
        holder.nameTextView.text = "${user.nome} ${user.cognome}"
        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(user.profileImage)
            .error(R.drawable.pippo_baudo)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (resource != null) {
                        user.profileImage = resource
                    }

                    return false
                }
            })
            .into(holder.profileImageView)
    }

    interface OnItemClickListener {
        fun onItemClick(user: User)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    override fun getItemCount(): Int {
        return userListFiltered.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = mutableListOf<User>()
                if (constraint.isNullOrBlank()) {
                    filteredList.addAll(userListAll)
                } else {
                    val query = constraint.toString().toLowerCase(Locale.ITALIAN)
                    for (user in userListAll) {
                        if (user.username.toLowerCase(Locale.ITALIAN).contains(query)) {
                            filteredList.add(user)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                userListFiltered = results?.values as List<User>
                notifyDataSetChanged()
            }
        }
    }
}
