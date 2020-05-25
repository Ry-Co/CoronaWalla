package com.example.coronawalla.main.ui.discussion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coronawalla.R
import kotlinx.android.synthetic.main.comment.view.*

class CommentsRecyclerViewAdapter(private val commentList: List<String>) : RecyclerView.Adapter<CommentsRecyclerViewAdapter.CommentsRecyclerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.comment, parent, false)
        return CommentsRecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommentsRecyclerViewHolder, position: Int) {
        var currentItem = commentList[position]
        var prevVote = false //getPrevVoteAndSetLocalConditions(currentItem,uid)
        var voteCount = 0


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

}