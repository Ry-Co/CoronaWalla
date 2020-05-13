package com.example.coronawalla.main.ui.local

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coronawalla.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.list_item.view.*

class RecyclerViewAdapter(private val postList: List<PostClass>) : RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder>() {
    private val changedPosts = ArrayList<PostClass>()
    private val TAG: String? = RecyclerViewAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return RecyclerViewHolder(
            itemView
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        var currentItem = postList[position]
        //todo make the uid call more robust, we are getting nullpointers sometimes
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ageHours = (System.currentTimeMillis() - currentItem.post_date_long) / 3600000 // milliseconds per hour
        var prevVote = getPrevVoteAndSetLocalConditions(currentItem,uid)


        holder.postTextTV.text = currentItem.post_text
        holder.postAgeTV.text = ageHours.toString() + "h"
        holder.voteCountTV.text = currentItem.vote_count.toString()
        voteVisual(holder, currentItem.users_vote)
        holder.voteCountTV.text = updateVoteCount(currentItem.vote_count.toString(), currentItem.users_vote, prevVote)

        holder.upvoteIV.setOnClickListener {
            currentItem.users_vote = vote(currentItem.users_vote, true, holder)
            val voteCountString = updateVoteCount(currentItem.vote_count.toString(), currentItem.users_vote, prevVote)
            holder.voteCountTV.text = voteCountString
            prevVote = currentItem.users_vote
            currentItem.vote_count = voteCountString.toInt()
            currentItem = updatePostLists(currentItem,uid)
            changedPosts.add(currentItem)
        }

        holder.downvoteIV.setOnClickListener {
            currentItem.users_vote = vote(currentItem.users_vote, false, holder)
            val voteCountString = updateVoteCount(currentItem.vote_count.toString(), currentItem.users_vote, prevVote)
            holder.voteCountTV.text = voteCountString
            prevVote = currentItem.users_vote
            currentItem.vote_count = voteCountString.toInt()
            currentItem = updatePostLists(currentItem,uid)
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

    private fun getPrevVoteAndSetLocalConditions(currentItem:PostClass, uid:String):Boolean?{

        return if(currentItem.votes_map!!.containsKey(uid)){
            when {
                currentItem.votes_map!![uid] == true -> {
                    currentItem.users_vote = true
                    true
                }
                currentItem.votes_map!![uid] == false -> {
                    currentItem.users_vote = false
                    false
                }
                else -> {
                    currentItem.users_vote = null
                    null
                }
            }
        }else{
            currentItem.users_vote = null
            null
        }
    }

    private fun updatePostLists(currentItem: PostClass, uid:String):PostClass{
        return when (currentItem.users_vote) {
            null -> {
                //we are using this instead of replace for api requirements
                currentItem.votes_map!!.remove(uid)
                currentItem.votes_map!![uid] = null
                currentItem
            }
            true -> {
                currentItem.votes_map!!.remove(uid)
                currentItem.votes_map!![uid] = true
                currentItem
            }
            else -> {
                currentItem.votes_map!!.remove(uid)
                currentItem.votes_map!![uid] = false
                currentItem
            }
        }
    }

    public fun getChangedList(): ArrayList<PostClass>{
        return changedPosts
    }
}