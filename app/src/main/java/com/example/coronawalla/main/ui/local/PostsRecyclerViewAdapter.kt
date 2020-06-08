package com.example.coronawalla.main.ui.local

import android.content.Intent
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
import kotlinx.android.synthetic.main.list_item.view.post_karma_tv

class PostsRecyclerViewAdapter(private val postList: List<PostClass>, private val navController:NavController) : RecyclerView.Adapter<PostsRecyclerViewAdapter.PostsRecyclerViewHolder>() {
    private val changedPosts = ArrayList<PostClass>()
    private var usersVote:Boolean? = null
    private val TAG: String? = PostsRecyclerViewAdapter::class.simpleName
    private val db = FirebaseFirestore.getInstance()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return PostsRecyclerViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: PostsRecyclerViewHolder, position: Int) {
        val currentItem = postList[position]
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        navigation(holder)
        voting(holder, uid, currentItem)
        holder.postTextTV.text = currentItem.post_text
        db.collection("users").document(currentItem.poster_id).get().addOnCompleteListener {
            if(it.isSuccessful){
                val userDoc = it.result
                if(currentItem.post_anon){
                    holder.posterHandleTV.text = "@Anonymous"
                }else{
                    holder.posterHandleTV.text = "@"+userDoc!!.get("handle").toString()
                }
            }else{
                Log.e(TAG, it.exception.toString())
            }
        }
    }

    override fun getItemCount() = postList.size

    class PostsRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTextTV: TextView = itemView.postText_TV
        val postKarma: TextView = itemView.post_karma_tv
        val postAgeTV: TextView = itemView.duration_TV
        val postShare: TextView = itemView.post_share_tv
        val upvoteIV: ImageView = itemView.upvote_IV
        val downvoteIV: ImageView = itemView.downvote_IV
        val posterHandleTV:TextView = itemView.posters_handle_tv
    }

    fun getChangedList(): ArrayList<PostClass>{
        return changedPosts
    }

    private fun voting(holder:PostsRecyclerViewHolder, uid:String, currentPost:PostClass){
        val voteWorker = VoteWorker()
        var mult = 1
        if(!currentPost.post_anon){mult = 2}
        var prevVote = voteWorker.getPrevVote(uid, currentPost.votes_map!!)
        var voteCount = voteWorker.getVoteCount(currentPost.votes_map!!, mult)


        holder.postAgeTV.text = voteWorker.getAgeString(currentPost.post_date_long)
        holder.postKarma.text = voteCount.toString()

        voteWorker.voteVisual(holder.upvoteIV, holder.downvoteIV, prevVote)
        holder.postKarma.text = voteCount.toString()

        holder.upvoteIV.setOnClickListener {
            usersVote = voteWorker.vote(usersVote, true, holder.upvoteIV, holder.downvoteIV)
            currentPost.votes_map = voteWorker.updateVoteMap(usersVote, uid, currentPost.votes_map!!)
            val voteCountString = voteWorker.getVoteCount(currentPost.votes_map!!,mult).toString()
            holder.postKarma.text = voteCountString
            voteCount = voteCountString.toInt()
            prevVote = usersVote
            changedPosts.add(currentPost)
        }
        holder.downvoteIV.setOnClickListener {
            usersVote = voteWorker.vote(usersVote, false,holder.upvoteIV, holder.downvoteIV)
            currentPost.votes_map = voteWorker.updateVoteMap(usersVote, uid, currentPost.votes_map!!)
            val voteCountString = voteWorker.getVoteCount(currentPost.votes_map!!,mult).toString()
            holder.postKarma.text = voteCountString
            voteCount = voteCountString.toInt()
            prevVote = usersVote
            changedPosts.add(currentPost)
        }

        holder.postShare.setOnClickListener {
            //abbreviate post text, put it as title
            //put quotes around post text
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val shareBody = "'"+currentPost.post_text+"'"+"- posted on soapBox, join the conversation @ [playstoreLink]"
            val abbrString = currentPost.post_text.take(38) + "..."
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, abbrString)
            holder.itemView.context.startActivity(Intent.createChooser(shareIntent, "Share this post"))
        }


    }

    private fun navigation(holder:PostsRecyclerViewHolder){
        holder.itemView.setOnClickListener {
            goToDiscussion(holder.layoutPosition, holder.posterHandleTV.text.toString())
        }
        holder.postTextTV.setOnClickListener {
            goToDiscussion(holder.layoutPosition,holder.posterHandleTV.text.toString())
        }
    }

    private fun goToDiscussion(position:Int, posterHandle:String){
        Log.e(TAG, "Go to discussion")
        //add bundles
        //https://medium.com/incwell-innovations/passing-data-in-android-navigation-architecture-component-part-2-5f1ebc466935
        //val postReference = db.collection("posts").document(postList[position].post_id)
        val bundle = bundleOf("post" to postList[position], "posterHandle" to posterHandle)
        navController.navigate(R.id.action_local_to_discussionFragment, bundle)
    }
}