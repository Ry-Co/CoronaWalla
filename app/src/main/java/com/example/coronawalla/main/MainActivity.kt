package com.example.coronawalla.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.coronawalla.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        var navController = findNavController(R.id.main_nav_host_fragment)
        bottomNavigation.setupWithNavController(navController)
    }

}
