package com.example.coronawalla.main.ui.profile

import com.google.firebase.auth.FirebaseUser

data class UserClass(
    val mHandle: String,
    val mUsername: String,
    val mUserID: String,
    val mPostsCount: Int,
    val mKarmaCount: Int,
    val mFollowerCount: Int,
    val mFollowingCount: Int,
    val mNamedPostCount: Int,
    val mAnonPostCount: Int,
    val mRatio: Double,
    val mAuthUserObject: FirebaseUser
)