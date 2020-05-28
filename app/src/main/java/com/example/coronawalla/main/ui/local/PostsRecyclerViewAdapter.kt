package com.example.coronawalla.main.ui.local

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.coronawalla.R
import com.example.coronawalla.main.VoteWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.list_item.view.*

class PostsRecyclerViewAdapter(private val postList: List<PostClass>, private val navController:NavController) : RecyclerView.Adapter<PostsRecyclerViewAdapter.PostsRecyclerViewHolder>() {
    private val changedPosts = ArrayList<PostClass>()
    private var usersVote:Boolean? = null
    private val TAG: String? = PostsRecyclerViewAdapter::class.simpleName


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return PostsRecyclerViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: PostsRecyclerViewHolder, position: Int) {
        val currentItem = postList[position]
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        //val ageHours = (System.currentTimeMillis() - currentItem.post_date_long) / 3600000 // milliseconds per hour
        val voteWorker = VoteWorker()
        var prevVote = voteWorker.getPrevVote(uid, currentItem.votes_map!!)
        var voteCount = voteWorker.getVoteCount(currentItem.votes_map!!)


        holder.postTextTV.text = currentItem.post_text
        holder.postAgeTV.text = voteWorker.getAgeString(currentItem.post_date_long)
        holder.voteCountTV.text = voteCount.toString()

        voteWorker.voteVisual(holder.upvoteIV, holder.downvoteIV, prevVote)
        holder.voteCountTV.text = voteCount.toString()

        holder.upvoteIV.setOnClickListener {
            usersVote = voteWorker.vote(usersVote, true, holder.upvoteIV, holder.downvoteIV)
            val voteCountString = voteWorker.updateVoteCountString(usersVote, prevVote, voteCount.toString())
            holder.voteCountTV.text = voteCountString
            prevVote = usersVote
            voteCount = voteCountString.toInt()
            currentItem.votes_map = voteWorker.updateVoteMap(usersVote,uid, currentItem.votes_map!!)
            changedPosts.add(currentItem)
        }

        holder.downvoteIV.setOnClickListener {
            usersVote = voteWorker.vote(usersVote, false,holder.upvoteIV, holder.downvoteIV)
            val voteCountString = voteWorker.updateVoteCountString(usersVote,prevVote,voteCount.toString())
            holder.voteCountTV.text = voteCountString
            prevVote = usersVote
            voteCount = voteCountString.toInt()
            currentItem.votes_map = voteWorker.updateVoteMap(usersVote, uid, currentItem.votes_map!!)
            changedPosts.add(currentItem)
        }

        FirebaseFirestore.getInstance().collection("users").document(currentItem.poster_id).get().addOnCompleteListener {
            if(it.isSuccessful){
                val userDoc = it.result
                holder.posterHandleTV.text = "@"+userDoc!!.get("handle").toString()
            }else{
                Log.e(TAG, it.exception.toString())
            }
        }

        holder.itemView.setOnClickListener {
            goToDiscussion(holder.layoutPosition, holder.posterHandleTV.text.toString())
        }
        holder.postTextTV.setOnClickListener {
            goToDiscussion(holder.layoutPosition,holder.posterHandleTV.text.toString())
        }

    }

    override fun getItemCount() = postList.size

    class PostsRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTextTV: TextView = itemView.postText_TV
        val voteCountTV: TextView = itemView.voteCounter_TV
        val postAgeTV: TextView = itemView.duration_TV
        val upvoteIV: ImageView = itemView.upvote_IV
        val downvoteIV: ImageView = itemView.downvote_IV
        val posterHandleTV:TextView = itemView.posters_handle_tv
    }

    public fun getChangedList(): ArrayList<PostClass>{
        return changedPosts
    }

    private fun goToDiscussion(position:Int, posterHandle:String){
        Log.e(TAG, "Go to discussion")
        //add bundles
        //https://medium.com/incwell-innovations/passing-data-in-android-navigation-architecture-component-part-2-5f1ebc466935
        val postReference = FirebaseFirestore.getInstance().collection("posts").document(postList[position].post_id)
        val bundle = bundleOf("post" to postList[position], "posterHandle" to posterHandle)
        navController.navigate(R.id.action_local_to_discussionFragment, bundle)

    }
}