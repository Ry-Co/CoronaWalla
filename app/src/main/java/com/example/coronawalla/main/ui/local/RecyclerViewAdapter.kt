package com.example.coronawalla.main.ui.local

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.example.coronawalla.R
import com.example.coronawalla.main.MainActivityViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.list_item.view.*

class RecyclerViewAdapter(private val postList: List<PostClass>) : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {
    private val changedPosts = ArrayList<PostClass>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return RecyclerViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentItem = postList[position]
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ageHours = (System.currentTimeMillis() - currentItem.mPostDateLong) / 3600000 // milliseconds per hour
        var prevVote: Boolean? = null

        prevVote = when {
            currentItem.mUpvoteIDs.contains(uid) -> {
                true
            }
            currentItem.mDownvoteIDs.contains(uid) -> {
                false
            }
            else -> {
                null
            }
        }

        holder.postTextTV.text = currentItem.mPostText
        holder.postAgeTV.text = ageHours.toString() + "h"
        holder.voteCountTV.text = currentItem.mVoteCount.toString()
        voteVisual(holder, currentItem.mUserVote)
        holder.voteCountTV.text = updateVoteCount(currentItem.mVoteCount.toString(), currentItem.mUserVote, prevVote)

        holder.upvoteIV.setOnClickListener {
            currentItem.mUserVote = vote(currentItem.mUserVote, true, holder)
            val voteCountString = updateVoteCount(currentItem.mVoteCount.toString(), currentItem.mUserVote, prevVote)
            prevVote = currentItem.mUserVote
            currentItem.mVoteCount = voteCountString.toInt()
            if(currentItem.mUserVote == null){
                if(currentItem.mUpvoteIDs.contains(uid)){currentItem.mUpvoteIDs.remove(uid)}
                if(currentItem.mDownvoteIDs.contains(uid)){currentItem.mDownvoteIDs.remove(uid)}
            }else if(currentItem.mUserVote == true){
                if(!currentItem.mUpvoteIDs.contains(uid)){currentItem.mUpvoteIDs.add(uid)}
                if(currentItem.mDownvoteIDs.contains(uid)){currentItem.mDownvoteIDs.remove(uid)}
            }else{
                if(currentItem.mUpvoteIDs.contains(uid)){currentItem.mUpvoteIDs.remove(uid)}
                if(!currentItem.mDownvoteIDs.contains(uid)){currentItem.mDownvoteIDs.add(uid)}
            }
            holder.voteCountTV.text = voteCountString
            changedPosts.add(currentItem)
        }

        holder.downvoteIV.setOnClickListener {
            currentItem.mUserVote = vote(currentItem.mUserVote, false, holder)
            val voteCountString = updateVoteCount(currentItem.mVoteCount.toString(), currentItem.mUserVote, prevVote)
            prevVote = currentItem.mUserVote
            currentItem.mVoteCount = voteCountString.toInt()
            if(currentItem.mUserVote == null){
                if(currentItem.mUpvoteIDs.contains(uid)){currentItem.mUpvoteIDs.remove(uid)}
                if(currentItem.mDownvoteIDs.contains(uid)){currentItem.mDownvoteIDs.remove(uid)}
            }else if(currentItem.mUserVote == true){
                if(!currentItem.mUpvoteIDs.contains(uid)){currentItem.mUpvoteIDs.add(uid)}
                if(currentItem.mDownvoteIDs.contains(uid)){currentItem.mDownvoteIDs.remove(uid)}
            }else{
                if(currentItem.mUpvoteIDs.contains(uid)){currentItem.mUpvoteIDs.remove(uid)}
                if(!currentItem.mDownvoteIDs.contains(uid)){currentItem.mDownvoteIDs.add(uid)}
            }
            holder.voteCountTV.text = voteCountString
            changedPosts.add(currentItem)
        }
    }

    override fun getItemCount() = postList.size

    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTextTV: TextView = itemView.postText_TV
        val voteCountTV: TextView = itemView.voteCounter_TV
        val postAgeTV: TextView = itemView.duration_TV
        val upvoteIV: ImageView = itemView.upvote_IV
        val downvoteIV: ImageView = itemView.downvote_IV
    }

    private fun vote(state: Boolean?, action: Boolean, holder: RecyclerViewHolder): Boolean? {
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

    private fun voteVisual(holder: RecyclerViewHolder, vote: Boolean?) {
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

    public fun getChangedList(): ArrayList<PostClass>{
        return changedPosts
    }
}