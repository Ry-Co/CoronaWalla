package com.example.coronawalla.main.ui.discussion

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentClass(
    val comment_id: String = "",
    val commenter_handle: String = "",
    val commenter_id: String = "",
    val comment_text: String = "",
    var comment_votes_map: MutableMap<String, Boolean?>? = null
) : Parcelable