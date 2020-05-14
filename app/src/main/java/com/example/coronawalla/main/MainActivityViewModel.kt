package com.example.coronawalla.main

import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coronawalla.main.ui.local.PostClass
import com.example.coronawalla.main.ui.profile.UserClass
import com.google.firebase.firestore.FirebaseFirestore

//https://proandroiddev.com/when-to-load-data-in-viewmodels-ad9616940da7

class MainActivityViewModel: ViewModel() {
    private val TAG: String? = MainActivityViewModel::class.simpleName
    val db = FirebaseFirestore.getInstance()
    var toolbarMode = MutableLiveData<Int>()
    var currentLocation = MutableLiveData<Location>()
    var currentUser = MutableLiveData<UserClass>()
    var currentProfileBitmap = MutableLiveData<Bitmap>()
    var localPostList = MutableLiveData<ArrayList<PostClass>>()
    var updateUserServer = MutableLiveData<Boolean>()
    val takenHandleSet = hashSetOf<String>()




}