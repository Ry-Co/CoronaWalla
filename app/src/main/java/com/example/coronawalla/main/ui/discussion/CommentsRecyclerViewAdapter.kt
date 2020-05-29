package com.example.coronawalla.main.ui.discussion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coronawalla.R
import com.example.coronawalla.main.VoteWorker
import com.google.firebase.firestore.FirebaseFirestore
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
        val currentItem = commentList[position]
        val uid = currentItem.commenter_id
        voting(holder, uid, currentItem)
        holder.commentTextTV.text = currentItem.comment_text
        FirebaseFirestore.getInstance().collection("users").document(currentItem.commenter_id).get().addOnSuccessListener {
            holder.commentersHandleTV.text = "@"+it.get("handle")
            currentItem.commenter_handle = it.get("handle").toString()
        }
    }

    override fun getItemCount() = commentList.size

    class CommentsRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val commentTextTV: TextView = itemView.comment_text_tv
        val commentersHandleTV : TextView = itemView.commenters_handle_tv
        val commentKarmaTV: TextView = itemView.comment_karma_tv
        val upvoteIV:ImageView = itemView.comment_upvote_iv
        val downvoteIV : ImageView = itemView.comment_downvote_iv
    }

    fun getChangedList(): ArrayList<CommentClass>{
        return changedComments
    }

    private fun voting(holder:CommentsRecyclerViewHolder, uid:String, currentComment:CommentClass){
        val voteWorker = VoteWorker()
        var prevVote = voteWorker.getPrevVote(uid, currentComment.votes_map!!)
        var voteCount = voteWorker.getVoteCount(currentComment.votes_map!!)

        voteWorker.voteVisual(holder.upvoteIV, holder.downvoteIV, prevVote)
        holder.commentKarmaTV.text = voteCount.toString()

        holder.upvoteIV.setOnClickListener {
            usersVote = voteWorker.vote(usersVote, true, holder.upvoteIV, holder.downvoteIV)
            currentComment.votes_map = voteWorker.updateVoteMap(usersVote, uid, currentComment.votes_map!!)
            val voteCountString = voteWorker.getVoteCount(currentComment.votes_map!!).toString()
            holder.commentKarmaTV.text = voteCountString
            voteCount = voteCountString.toInt()
            prevVote = usersVote
            changedComments.add(currentComment)
        }
        holder.downvoteIV.setOnClickListener {
            usersVote = voteWorker.vote(usersVote, false,holder.upvoteIV, holder.downvoteIV)
            currentComment.votes_map = voteWorker.updateVoteMap(usersVote, uid, currentComment.votes_map!!)
            val voteCountString = voteWorker.getVoteCount(currentComment.votes_map!!).toString()
            holder.commentKarmaTV.text = voteCountString
            voteCount = voteCountString.toInt()
            prevVote = usersVote
            changedComments.add(currentComment)
        }
    }
}