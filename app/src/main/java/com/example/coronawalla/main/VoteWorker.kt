package com.example.coronawalla.main

import android.widget.ImageView
import com.example.coronawalla.R
import com.example.coronawalla.main.ui.discussion.CommentClass
import com.example.coronawalla.main.ui.local.PostClass
import kotlin.math.round

class VoteWorker() {
    val postComparator =  Comparator<PostClass> { a, b ->
        var aMult = 1
        var bMult = 1
        if(!a.post_anon){
            aMult = 2
        }
        if(!b.post_anon){
            bMult = 2
        }
        val vw = VoteWorker()
        val tic = System.currentTimeMillis()
        val aVoteCount = vw.getVoteCount(a.votes_map!!, aMult)
        val bVoteCount = vw.getVoteCount(b.votes_map!!, bMult)
        val aAgeHours  = (tic - a.post_date_long) / 3600000.0
        val aRate = aVoteCount/aAgeHours
        val bAgeHours  = (tic - b.post_date_long) / 3600000.0
        val bRate = bVoteCount/bAgeHours
        //Log.e(TAG, "a ="+a.post_id+" b ="+b.post_id+" aRate= $aRate  bRate= $bRate")
        when {
            aRate == bRate -> {
                //Log.e(TAG, "EQUAL")
                0
            }
            aRate < bRate -> {
                //Log.e(TAG, "a < b")
                1
            }
            else -> {
                //Log.e(TAG, "a > b")
                -1
            }
        }
    }
    val commentComparator =  Comparator<CommentClass> { a, b ->
        var aMult = 1
        var bMult = 1
        if(!a.comment_anon){
            aMult = 2
        }
        if(!b.comment_anon){
            bMult = 2
        }
        val vw = VoteWorker()
        val tic = System.currentTimeMillis()
        val aVoteCount = vw.getVoteCount(a.votes_map!!, aMult)
        val bVoteCount = vw.getVoteCount(b.votes_map!!, bMult)
        val aAgeHours  = (tic - a.comment_date_long) / 3600000.0
        val aRate = aVoteCount/aAgeHours
        val bAgeHours  = (tic - b.comment_date_long) / 3600000.0
        val bRate = bVoteCount/bAgeHours
        //Log.e(TAG, "a ="+a.post_id+" b ="+b.post_id+" aRate= $aRate  bRate= $bRate")
        when {
            aRate == bRate -> {
                //Log.e(TAG, "EQUAL")
                0
            }
            aRate < bRate -> {
                //Log.e(TAG, "a < b")
                1
            }
            else -> {
                //Log.e(TAG, "a > b")
                -1
            }
        }
    }

    fun updateVoteMap(usersVote: Boolean?, uid: String, votes_map: MutableMap<String, Boolean?>): MutableMap<String, Boolean?> {
        return when (usersVote) {
            null -> {
                //we are using this instead of replace for api requirements
                votes_map.remove(uid)
                votes_map[uid] = null
                votes_map
            }
            true -> {
                votes_map.remove(uid)
                votes_map[uid] = true
                votes_map
            }
            else -> {
                votes_map.remove(uid)
                votes_map[uid] = false
                votes_map
            }
        }
    }

    fun vote(state: Boolean?, action: Boolean, upvoteIV: ImageView, downvoteIV: ImageView): Boolean? {
        when (state) {
            null -> return when (action) {
                true -> {
                    //upvote +1
                    voteVisual(upvoteIV, downvoteIV, true)
                    true
                }
                false -> {
                    //downvote -1
                    voteVisual(upvoteIV, downvoteIV, false)
                    false
                }
            }
            false -> return when (action) {
                true -> {
                    //downvote + upvote = upvote +2
                    voteVisual(upvoteIV, downvoteIV, true)
                    true
                }
                false -> {
                    //downvote + downvote = no vote +1
                    voteVisual(upvoteIV, downvoteIV, null)
                    null
                }
            }
            true -> return when (action) {
                true -> {
                    //upvote + upvote = no vote -1
                    voteVisual(upvoteIV, downvoteIV, null)
                    null
                }
                false -> {
                    //upvote + downvote = downvote -2
                    voteVisual(upvoteIV, downvoteIV, false)
                    false
                }
            }
        }
    }

    fun getPrevVote(uid: String, votes_map: MutableMap<String, Boolean?>): Boolean? {
        return if (votes_map.containsKey(uid)) {
            when {
                votes_map[uid] == true -> {
                    true
                }
                votes_map[uid] == false -> {
                    false
                }
                else -> {
                    null
                }
            }
        } else {
            null
        }
    }

    fun getVoteCount(votes_map: MutableMap<String, Boolean?>, multiplier:Int): Int {
        return if (votes_map.isEmpty()) {
            //do nothing
            0
        } else {
            var counter = 0
            for (item in votes_map) {
                when (item.value) {
                    true -> {
                        //upvote
                        counter += 1
                    }
                    false -> {
                        //downvote
                        counter -= 1
                    }
                    else -> {
                        //no vote
                        counter += 0
                    }
                }
            }
            counter*multiplier
        }
    }

    fun voteVisual(upvoteIV: ImageView, downvoteIV: ImageView, vote: Boolean?) {
        println("Vote status:: " + vote)
        when (vote) {
            null -> {
                upvoteIV.setImageResource(R.drawable.ic_arrow_upward_black_24dp)
                downvoteIV.setImageResource(R.drawable.ic_arrow_downward_black_24dp)
            }
            true -> {
                upvoteIV.setImageResource(R.drawable.ic_arrow_upward_green_24dp)
                downvoteIV.setImageResource(R.drawable.ic_arrow_downward_black_24dp)
            }
            false -> {
                upvoteIV.setImageResource(R.drawable.ic_arrow_upward_black_24dp)
                downvoteIV.setImageResource(R.drawable.ic_arrow_downward_red_24dp)
            }
        }
    }

    fun getAgeString(postDate: Long): String {
        val hours = (System.currentTimeMillis() - postDate) / 3600000 // milliseconds per hour

        return when {
            hours < 1 -> {
                val minutes = (System.currentTimeMillis() - postDate) / 60000
                minutes.toInt().toString() + "min"
            }
            hours < 24 -> {
                hours.toInt().toString() + "h"
            }
            hours / 24 < 30 -> {
                val daysLong = hours / 24
                val days = round(daysLong.toDouble())
                days.toInt().toString() + "d"
            }

            else -> {
                val daysLong = hours / 24
                val days = round(daysLong.toDouble())
                val weeks = round(days / 7)
                weeks.toInt().toString() + "w"
            }
        }

    }

}