package com.example.coronawalla.main.ui.profile

import com.example.coronawalla.main.ui.local.PostClass

data class UserClass(
    var handle: String? = null,
    var username: String? = null,
    var user_id: String = "",
    var posts: MutableList<PostClass> = mutableListOf(),
    var karma:Int = 0,
    val followers: MutableList<String> = mutableListOf(),
    val following: MutableList<String> = mutableListOf(),
    val ratio: Double = 0.0,
    var profile_image_url: String? = null
)