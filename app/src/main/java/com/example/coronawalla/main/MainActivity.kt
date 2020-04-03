package com.example.coronawalla.main

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.coronawalla.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy{
        this.let { ViewModelProviders.of(it).get(MainActivityViewModel::class.java) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        //bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val navController = findNavController(R.id.main_nav_host_fragment)
        bottomNavigation.setupWithNavController(navController)

        viewModel.toolbarMode.observe(this, Observer {
            when(viewModel.toolbarMode.value){
                 1 -> roamToolbar() //roam
                 0 -> localToolbar() //local
                -1 -> profileToolbar() //profile
                 2 -> postToolbar() //post creation toolbar
            }
        })
    }

    private fun profileToolbar(){
        toolbar_title_tv.text = "Profile"
        post_IV.visibility = View.INVISIBLE
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
    }
    private fun localToolbar(){
        toolbar_title_tv.text = "Local"
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
        post_IV.visibility = View.VISIBLE
        post_IV.setOnClickListener{
            findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_local_to_postFragment)
            Toast.makeText(this, "POST", Toast.LENGTH_SHORT).show()
        }
    }
    private fun roamToolbar(){
        toolbar_title_tv.text = "Roam"
        toolbar_send_tv.visibility = View.INVISIBLE
        toolbar_cancel_tv.visibility = View.INVISIBLE
        post_IV.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.VISIBLE
    }
    private fun postToolbar(){
        toolbar_title_tv.text = "Post"
        post_IV.visibility = View.INVISIBLE
        bottomNavigation.visibility = View.INVISIBLE
        toolbar_send_tv.visibility = View.VISIBLE
        toolbar_cancel_tv.visibility = View.VISIBLE
        toolbar_cancel_tv.setOnClickListener {
            findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_postFragment_to_local)
        }



    }
}
