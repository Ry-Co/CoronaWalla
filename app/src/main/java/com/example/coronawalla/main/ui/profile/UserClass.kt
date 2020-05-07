package com.example.coronawalla.main.ui.profile

import com.example.coronawalla.main.ui.local.PostClass

data class UserClass(
    val handle: String = "NoHandle",
    val username: String = "Anonymous",
    var user_id: String = "",
    var posts: MutableList<PostClass> = mutableListOf(),
    var karma:Int = 0,
    val followers_count: Int= 0,
    val following_count: Int= 0,
    val ratio: Double = 0.0,
    var profile_image_url: String? = null
)