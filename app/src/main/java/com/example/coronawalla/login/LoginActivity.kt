package com.example.coronawalla.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coronawalla.R
import com.example.coronawalla.login.ui.CoverFragment

class LoginActivity : AppCompatActivity(){
    private lateinit var coverFragment: CoverFragment

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        checkUser()
        setContentView(R.layout.activity_login)
        coverFragment = CoverFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.login_fragment_container, coverFragment)
            .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

    }

    override fun onStart() {
        super.onStart()
        checkUser()
    }

    private fun checkUser(){

    }





}
