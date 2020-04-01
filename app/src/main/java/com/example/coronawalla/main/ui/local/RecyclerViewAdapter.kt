package com.example.coronawalla.main.ui.local

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coronawalla.R
import kotlinx.android.synthetic.main.list_item.view.*

class RecyclerViewAdapter(private val postList: List<PostClass>) : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return RecyclerViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val currentItem = postList[position]
        val ageHours = (System.currentTimeMillis() - currentItem.postDateLong)/3600000 // milliseconds per hour
        holder.postTextTV.text = currentItem.mPostText
        holder.postAgeTV.text = ageHours.toString()+"h"
        holder.voteCountTV.text = currentItem.mVoteCount
        //vote handeling, order is null first for efficency
        when(currentItem.userVote){
            null -> print("user has not voted")
            false -> print("user has downvoted")
            true -> print("user has upvoted")
        }

        //set the on click listeners in here in the holders
        holder.upvoteIV.setOnClickListener{

        }
        holder.downvoteIV.setOnClickListener{

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
}