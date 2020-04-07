package com.example.coronawalla.main

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coronawalla.main.ui.local.PostClass
import com.google.firebase.firestore.DocumentSnapshot

class MainActivityViewModel: ViewModel(){
    var toolbarMode = MutableLiveData<Int>()
    var currentLocation = MutableLiveData<Location>()
    var localDocList = MutableLiveData<List<DocumentSnapshot>>()
}