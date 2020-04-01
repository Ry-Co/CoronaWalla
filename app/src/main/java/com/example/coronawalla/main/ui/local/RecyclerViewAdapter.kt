package com.example.coronawalla.main.ui.local

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coronawalla.R
import kotlinx.android.synthetic.main.list_item.view.*

class RecyclerViewAdapter(private val postList: List<PostClass>) :
    RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return RecyclerViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentItem = postList[position]
        val ageHours = (System.currentTimeMillis() - currentItem.postDateLong) / 3600000 // milliseconds per hour
        holder.postTextTV.text = currentItem.mPostText
        holder.postAgeTV.text = ageHours.toString() + "h"
        holder.voteCountTV.text = currentItem.mVoteCount
        voteVisual(holder, currentItem.userVote)
        holder.voteCountTV.text = updateVoteCount(currentItem.mVoteCount, currentItem.userVote)

        holder.upvoteIV.setOnClickListener {
            currentItem.userVote = vote(currentItem.userVote, true, holder)
            holder.voteCountTV.text = updateVoteCount(currentItem.mVoteCount,currentItem.userVote)
        }

        holder.downvoteIV.setOnClickListener {
            currentItem.userVote =  vote(currentItem.userVote, false, holder)
            holder.voteCountTV.text = updateVoteCount(currentItem.mVoteCount,currentItem.userVote)
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

    private fun vote(state: Boolean?, action: Boolean, holder:RecyclerViewHolder): Boolean?{
        when (state) {
            null -> return when (action) {
                true ->{
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
        println("Vote status:: "+vote)
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

    private fun updateVoteCount(postVoteCount:String, usersVote:Boolean?): String{
        var pvNum = postVoteCount.toInt()
        return when(usersVote){
            null ->{
                postVoteCount
            }
            true ->{
                pvNum += 1
                pvNum.toString()
            }
            false ->{
                pvNum -= 1
                pvNum.toString()
            }
        }
    }

}