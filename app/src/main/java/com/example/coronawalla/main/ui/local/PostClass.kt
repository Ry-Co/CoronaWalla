package com.example.coronawalla.main.ui.local

import com.google.firebase.firestore.GeoPoint


data class PostClass(
    val post_id: String = "",
    val post_text: String = "",
    val poster_id : String = "",
    val active: Boolean? = null,
    val post_geo_point: GeoPoint? = null,
    val post_date_long: Long = 0,
    val post_multiplier: Int = 0,
    val payout_date_long: Long = 0,
    var votes_map:MutableMap<String,Boolean?>? = null,
    var vote_count:Int =0,
    var users_vote: Boolean?= null
)