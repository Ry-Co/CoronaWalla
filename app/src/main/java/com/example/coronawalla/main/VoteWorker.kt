package com.example.coronawalla.main

import android.widget.ImageView
import com.example.coronawalla.R
import kotlin.math.round

class VoteWorker() {
    fun updateVoteMap(
        usersVote: Boolean?,
        uid: String,
        votes_map: MutableMap<String, Boolean?>
    ): MutableMap<String, Boolean?> {
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

    fun updateVoteCountString(
        usersVote: Boolean?,
        usersPreviousVote: Boolean?,
        currentVoteCount: String
    ): String {
        var pvNum = currentVoteCount.toInt()
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

    fun vote(
        state: Boolean?,
        action: Boolean,
        upvoteIV: ImageView,
        downvoteIV: ImageView
    ): Boolean? {
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

    fun getVoteCount(votes_map: MutableMap<String, Boolean?>): Int {
        return if (votes_map.isEmpty()) {
            //do nothing
            0
        } else {
            var counter = 0
            for (item in votes_map) {
                if (item.value == true) {
                    counter += 1
                }
            }
            counter
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