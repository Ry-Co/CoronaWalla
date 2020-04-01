package com.example.coronawalla.main.ui.local

data class PostClass(val mPostText: String, val mVoteCount: String, val postDateLong: Long, var userVote: Boolean?= null)
//users vote will be null for no value, true if they have upvoted, false if they've downvoted
