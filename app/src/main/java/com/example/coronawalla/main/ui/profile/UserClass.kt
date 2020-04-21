package com.example.coronawalla.main.ui.profile

data class UserClass(
    val mHandle: String = "NoHandle",
    val mUsername: String = "Anonymous",
    val mUserID: String = "",
    val mPostsCount: Int = 0,
    val mKarmaCount: Int= 0,
    val mFollowerCount: Int= 0,
    val mFollowingCount: Int= 0,
    val mNamedPostCount: Int= 0,
    val mAnonPostCount: Int= 0,
    val mRatio: Double = 0.0,
    val mAuthUserObject: Map<*, *>? = null
)