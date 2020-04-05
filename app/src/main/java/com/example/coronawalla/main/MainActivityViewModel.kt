package com.example.coronawalla.main

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel(){
    var toolbarMode = MutableLiveData<Int>()
    var currentLocation = MutableLiveData<Location>()

}