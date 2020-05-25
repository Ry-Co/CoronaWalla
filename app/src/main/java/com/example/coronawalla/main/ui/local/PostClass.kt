package com.example.coronawalla.main.ui.local

import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class PostClass(
    val post_id: String = "",
    val post_text: String = "",
    val poster_id : String = "",
    val active: Boolean? = null,
    val post_geo_point: @RawValue GeoPoint? = null,
    val post_date_long: Long = 0,
    val post_multiplier: Int = 0,
    val payout_date_long: Long = 0,
    var votes_map:MutableMap<String,Boolean?>? = null,
    val g:String?="",
    val l:@RawValue GeoPoint?=null
) : Parcelable