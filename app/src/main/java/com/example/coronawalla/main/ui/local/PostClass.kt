package com.example.coronawalla.main.ui.local

/*
post text #
poster id #
post votes #
post location
post time #
payout time  1hr = 3600000miliseconds so post time + 3600000*24
list of userIDs who have upvoted #
list of userIDs who have udownvoted #
 */

data class PostClass(
    val mPostText: String,
    val mPosterID : String,
    val mVoteCount: Int,
    val postDateLong: Long,
    val upvoteIDs: HashSet<String>,
    var downvoteIDs: HashSet<String>,
    var userVote: Boolean?= null)
