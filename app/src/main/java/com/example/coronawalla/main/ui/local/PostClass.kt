package com.example.coronawalla.main.ui.local

import android.location.Location
import com.google.firebase.firestore.GeoPoint


data class PostClass(
    val mPostID: String,
    val mPostText: String,
    val mPosterID : String,
    val mPostGeoPoint: GeoPoint,
    val mVoteCount: Int,
    val mPostDateLong: Long,
    val mMultiplier: Int,
    val mPayoutDateLong: Long,
    val mUpvoteIDs: HashSet<String>,
    val mDownvoteIDs: HashSet<String>,
    var mUserVote: Boolean?= null) // deal with the initial vote in the recycler view