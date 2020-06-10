package com.example.coronawalla.main

import android.graphics.Bitmap
import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coronawalla.main.ui.local.PostClass
import com.example.coronawalla.main.ui.profile.UserClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

//https://proandroiddev.com/when-to-load-data-in-viewmodels-ad9616940da7

class MainActivityViewModel: ViewModel() {
    private val TAG: String? = MainActivityViewModel::class.simpleName
   // val db = FirebaseFirestore.getInstance()
   // val mAuth = FirebaseAuth.getInstance()
    val storage = FirebaseStorage.getInstance()
    var toolbarMode = MutableLiveData<Int>()
    var currentLocation = MutableLiveData<Location>()
    var currentUser = MutableLiveData<UserClass>()
    var currentProfileBitmap = MutableLiveData<Bitmap>()
    var localPostList = MutableLiveData<ArrayList<PostClass>>()


}