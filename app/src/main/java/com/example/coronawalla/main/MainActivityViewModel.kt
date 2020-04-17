package com.example.coronawalla.main

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coronawalla.main.ui.local.PostClass

class MainActivityViewModel: ViewModel() {
    var toolbarMode = MutableLiveData<Int>()
    var currentLocation = MutableLiveData<Location>()
    var localPostList = MutableLiveData<ArrayList<PostClass>>()
    //https://proandroiddev.com/when-to-load-data-in-viewmodels-ad9616940da7

}