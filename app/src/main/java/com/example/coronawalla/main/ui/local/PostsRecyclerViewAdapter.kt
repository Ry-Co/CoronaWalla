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
import com.google.firebase.auth.FirebaseAuth
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
        var currentItem = postList[position]
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ageHours = (System.currentTimeMillis() - currentItem.post_date_long) / 3600000 // milliseconds per hour
        var prevVote = getPrevVoteAndSetLocalConditions(currentItem,uid)
        var voteCount = 0
        if(currentItem.votes_map!!.isEmpty()){
            //do nothing
        }else{
            var counter = 0
            for(item in currentItem.votes_map!!){
                if(item.value == true){
                    counter += 1
                }
            }
            voteCount = counter
        }


        holder.postTextTV.text = currentItem.post_text
        holder.postAgeTV.text = ageHours.toString() + "h"
        holder.voteCountTV.text = voteCount.toString()
        voteVisual(holder, usersVote)
        holder.voteCountTV.text = updateVoteCount(voteCount.toString(), usersVote, prevVote)

        holder.upvoteIV.setOnClickListener {
            usersVote = vote(usersVote, true, holder)
            val voteCountString = updateVoteCount(voteCount.toString(), usersVote, prevVote)
            holder.voteCountTV.text = voteCountString
            prevVote = usersVote
            voteCount = voteCountString.toInt()
            currentItem = updatePostLists(currentItem,uid)
            changedPosts.add(currentItem)
        }

        holder.downvoteIV.setOnClickListener {
            usersVote = vote(usersVote, false, holder)
            val voteCountString = updateVoteCount(voteCount.toString(), usersVote, prevVote)
            holder.voteCountTV.text = voteCountString
            prevVote = usersVote
            voteCount = voteCountString.toInt()
            currentItem = updatePostLists(currentItem,uid)
            changedPosts.add(currentItem)
        }

        holder.itemView.setOnClickListener {
            Log.e(TAG, "Go to discussion")
            goToDiscussion(holder.layoutPosition)
        }
        holder.postTextTV.setOnClickListener {
            Log.e(TAG, "Go to discussion")
            goToDiscussion(holder.layoutPosition)
        }
    }

    override fun getItemCount() = postList.size

    class PostsRecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postTextTV: TextView = itemView.postText_TV
        val voteCountTV: TextView = itemView.voteCounter_TV
        val postAgeTV: TextView = itemView.duration_TV
        val upvoteIV: ImageView = itemView.upvote_IV
        val downvoteIV: ImageView = itemView.downvote_IV
    }

    private fun vote(state: Boolean?, action: Boolean, holder: PostsRecyclerViewHolder): Boolean? {
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

    private fun voteVisual(holder: PostsRecyclerViewHolder, vote: Boolean?) {
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
                    //currentItem.users_vote = true
                    usersVote = true
                    true
                }
                currentItem.votes_map!![uid] == false -> {
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

    private fun updatePostLists(currentItem: PostClass, uid:String):PostClass{
        return when (usersVote) {
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

    private fun goToDiscussion(position:Int){
        //add bundles
        //https://medium.com/incwell-innovations/passing-data-in-android-navigation-architecture-component-part-2-5f1ebc466935
        val bundle = bundleOf("post" to postList[position])

        navController.navigate(R.id.action_local_to_discussionFragment, bundle)

    }
}