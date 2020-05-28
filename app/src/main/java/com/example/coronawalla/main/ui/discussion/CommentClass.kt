package com.example.coronawalla.main.ui.discussion

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentClass(
    val comment_id: String = "",
    var commenter_handle: String = "",
    val commenter_id: String = "",
    val comment_text: String = "",
    var votes_map: MutableMap<String, Boolean?>? = null
) : Parcelable