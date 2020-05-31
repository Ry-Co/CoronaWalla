package com.example.coronawalla.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivityViewModel : ViewModel() {
    var email : String? = null
    var password : String? = null
    var phoneNumber : String? = null
    var isSignIn : Boolean = false
    var goToPhone:Boolean = false
    val db = FirebaseFirestore.getInstance()
}