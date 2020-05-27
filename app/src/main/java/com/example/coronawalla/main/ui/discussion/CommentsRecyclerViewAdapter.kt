package com.example.coronawalla.main.ui.discussion

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coronawalla.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.comment.view.*

class CommentsRecyclerViewAdapter(private val commentList: List<CommentClass>) : RecyclerView.Adapter<CommentsRecyclerViewAdapter.CommentsRecyclerViewHolder>() {
    private val changedComments = ArrayList<CommentClass>()
    private val TAG: String? = CommentsRecyclerViewAdapter::class.simpleName
    private var usersVote:Boolean? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.comment, parent, false)
        return CommentsRecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentsRecyclerViewHolder, position: Int) {
        var currentItem = commentList[position]
        val uid = currentItem.commenter_id
        var prevVote = getPrevVoteAndSetLocalConditions(currentItem,uid)
        var voteCount = 0
        val ONE_MEGABYTE: Long = 1024 * 1024
        val profRef = FirebaseStorage.getInstance().reference.child("images/$uid")

        profRef.getBytes(ONE_MEGABYTE).addOnCompleteListener {
            if( it.isSuccessful){
                val bmp = BitmapFactory.decodeByteArray(it.result, 0, it.result!!.size)
                holder.commentersProfIV.setImageBitmap(bmp)
            }else{
                Log.e(TAG, it.exception.toString())
            }
        }

        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener{
            if(it.isSuccessful){
                val userDoc = it.result
                holder.commentersHandleTV.text = userDoc!!.get("handle").toString()
            }else{
                Log.e(TAG, it.exception.toString())
            }
        }

        if(currentItem.comment_votes_map!!.isEmpty()){
            //do nothing
        }else{
            var counter = 0
            for(item in currentItem.comment_votes_map!!){
                if(item.value == true){
                    counter += 1
                }
            }
            voteCount = counter
        }

        holder.commentTextTV.text = currentItem.comment_text
        voteVisual(holder, usersVote)
        holder.voteCountTV.text = updateVoteCount(voteCount.toString(), usersVote, prevVote)

        holder.upvoteIV.setOnClickListener {
            usersVote = vote(usersVote, true, holder)
            val voteCountString = updateVoteCount(voteCount.toString(), usersVote, prevVote)
            holder.voteCountTV.text = voteCountString
            prevVote = usersVote
            voteCount = voteCountString.toInt()
            currentItem = updatePostLists(currentItem,uid)
            changedComments.add(currentItem)
        }
        holder.downvoteIV.setOnClickListener {
            usersVote = vote(usersVote, false, holder)
            val voteCountString = updateVoteCount(voteCount.toString(), usersVote, prevVote)
            holder.voteCountTV.text = voteCountString
            prevVote = usersVote
            voteCount = voteCountString.toInt()
            currentItem = updatePostLists(currentItem,uid)
            changedComments.add(currentItem)
        }



        holder.replyTV.setOnClickListener {  }
    }

    override fun getItemCount() = commentList.size

    class CommentsRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val commentTextTV: TextView = itemView.comment_text_tv
        val voteCountTV: TextView = itemView.comment_karma_tv
        val replyTV: TextView = itemView.comment_reply_tv
        val upvoteIV:ImageView = itemView.comment_upvote_iv
        val downvoteIV : ImageView = itemView.comment_downvote_iv
        val commentersHandleTV : TextView = itemView.commenters_handle_tv
        val commentersProfIV: ImageView = itemView.commenter_prof_iv

    }

    private fun getPrevVoteAndSetLocalConditions(currentItem:CommentClass, uid:String):Boolean?{
        return if(currentItem.comment_votes_map!!.containsKey(uid)){
            when {
                currentItem.comment_votes_map!![uid] == true -> {
                    //currentItem.users_vote = true
                    usersVote = true
                    true
                }
                currentItem.comment_votes_map!![uid] == false -> {
                    //currentItem.users_vote = false
                    usersVote = false
                    false
                }
                else -> {
                    //currentItem.users_vote = null
                    usersVote = null
                    null
                }
            }
        }else{
            //currentItem.users_vote = null
            usersVote = null
            null
        }
    }


    private fun vote(state: Boolean?, action: Boolean, holder: CommentsRecyclerViewHolder): Boolean? {
        when (state) {
            null -> return when (action) {
                true -> {
                    //upvote +1
                    voteVisual(holder, true)
                    true
                }
                false -> {
                    //downvote -1
                    voteVisual(holder, false)
                    false
                }
            }
            false -> return when (action) {
                true -> {
                    //downvote + upvote = upvote +2
                    voteVisual(holder, true)
                    true
                }
                false -> {
                    //downvote + downvote = no vote +1
                    voteVisual(holder, null)
                    null
                }
            }
            true -> return when (action) {
                true -> {
                    //upvote + upvote = no vote -1
                    voteVisual(holder, null)
                    null
                }
                false -> {
                    //upvote + downvote = downvote -2
                    voteVisual(holder, false)
                    false
                }
            }
        }
    }

    private fun voteVisual(holder: CommentsRecyclerViewAdapter.CommentsRecyclerViewHolder, vote: Boolean?) {
        println("Vote status:: " + vote)
        when (vote) {
            null -> {
                holder.upvoteIV.setImageResource(R.drawable.ic_arrow_upward_black_24dp)
                holder.downvoteIV.setImageResource(R.drawable.ic_arrow_downward_black_24dp)
            }
            true -> {
                holder.upvoteIV.setImageResource(R.drawable.ic_arrow_upward_green_24dp)
                holder.downvoteIV.setImageResource(R.drawable.ic_arrow_downward_black_24dp)
            }
            false -> {
                holder.upvoteIV.setImageResource(R.drawable.ic_arrow_upward_black_24dp)
                holder.downvoteIV.setImageResource(R.drawable.ic_arrow_downward_red_24dp)
            }
        }
    }

    private fun updateVoteCount(postVoteCount: String, usersVote: Boolean?, usersPreviousVote: Boolean?): String {
        var pvNum = postVoteCount.toInt()
        return when (usersVote) {
            null -> {
                when (usersPreviousVote) {
                    null -> {
                        //user hasn't voted on post previously
                        pvNum.toString()
                    }
                    true -> {
                        //user previously upvoted post
                        pvNum -= 1
                        pvNum.toString()
                    }
                    else -> {
                        //user previously downvoted post
                        pvNum += 1
                        pvNum.toString()
                    }
                }
            }
            true -> {
                when (usersPreviousVote) {
                    null -> {
                        //user hasn't voted on post previously
                        pvNum += 1
                        pvNum.toString()
                    }
                    true -> {
                        //user previously upvoted post
                        //pvNum += 1
                        pvNum.toString()
                    }
                    else -> {
                        //user previously downvoted post
                        pvNum += 2
                        pvNum.toString()
                    }
                }
            }
            false -> {
                when (usersPreviousVote) {
                    null -> {
                        //user hasn't voted on post previously
                        pvNum -= 1
                        pvNum.toString()
                    }
                    true -> {
                        //user previously upvoted post
                        pvNum -= 2
                        pvNum.toString()
                    }
                    else -> {
                        //user previously downvoted post
                        //pvNum += 2
                        pvNum.toString()
                    }
                }
            }
        }
    }

    public fun getChangedList(): ArrayList<CommentClass>{
        return changedComments
    }

    private fun updatePostLists(currentItem: CommentClass, uid:String): CommentClass {
        return when (usersVote) {
            null -> {
                //we are using this instead of replace for api requirements
                currentItem.comment_votes_map!!.remove(uid)
                currentItem.comment_votes_map!![uid] = null
                currentItem
            }
            true -> {
                currentItem.comment_votes_map!!.remove(uid)
                currentItem.comment_votes_map!![uid] = true
                currentItem
            }
            else -> {
                currentItem.comment_votes_map!!.remove(uid)
                currentItem.comment_votes_map!![uid] = false
                currentItem
            }
        }
    }

}