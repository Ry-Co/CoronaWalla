package com.example.coronawalla.main

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.findNavController
import com.example.coronawalla.R
import com.example.coronawalla.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ToolbarWorker constructor(activity: Activity, userIsAnon:Boolean){
    private val TAG: String? = ToolbarWorker::class.simpleName
    private val anonUser:Boolean = userIsAnon
    private val mActivity: Activity = activity
    private val toolbarTitleTV = mActivity.findViewById<TextView>(R.id.toolbar_title_tv)
    private val toolbarSendTv = mActivity.findViewById<TextView>(R.id.toolbar_send_tv)
    private val toolbarCancelTv = mActivity.findViewById<TextView>(R.id.toolbar_cancel_tv)
    private val rightImageButton =mActivity.findViewById<ImageView>(R.id.right_button_iv)
    private val leftImageButton =mActivity.findViewById<ImageView>(R.id.left_button_iv)

    fun switchBox(int:Int){
        when(int){
            1 -> roamToolbar() //roam
            0 -> localToolbar() //local
            -1 -> profileToolbar() //profile
            -2 -> profileEditToolbar() // profile edit
            2 -> postToolbar() //post creation toolbar
            3 -> postPreviewToolbar() //preview
        }
    }

    private fun localToolbar(){
        Log.d(TAG, "Setting Toolbar to Local")
        toolbarTitleTV.text = "Curr. Town"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        rightImageButton.setImageResource(R.drawable.ic_create_white_24dp)
        leftImageButton.setImageResource(R.drawable.ic_person_white_24dp)
        rightImageButton.visibility = View.VISIBLE
        leftImageButton.visibility = View.VISIBLE

        rightImageButton.setOnClickListener {
            if(anonUser){
                //showSignInDialog()
                FirebaseAuth.getInstance().signOut()
            }else{
                mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_local_to_postFragment)
                Toast.makeText(mActivity, "POST", Toast.LENGTH_SHORT).show()
            }
        }
        leftImageButton.setOnClickListener {
            if(anonUser){
                showSignInDialog()
            }else{
                mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_local_to_profile)
            }
        }
        toolbarTitleTV.setOnClickListener {
            //todo start autocomplete activity
            Toast.makeText(mActivity, "OPEN TOWN SELECT", Toast.LENGTH_SHORT).show()
        }
    }

    private fun profileToolbar(){
        Log.d(TAG, "Setting Toolbar to Profile")
        toolbarTitleTV.text = "Profile"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        leftImageButton.setImageResource(R.drawable.ic_settings_white_24dp)
        rightImageButton.setImageResource(R.drawable.ic_arrow_forward_white_24dp)
        leftImageButton.visibility = View.VISIBLE
        rightImageButton.visibility = View.VISIBLE
        leftImageButton.setOnClickListener {
            Log.e(TAG, "Edit Profile!!!!")
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_profile_to_profileEditFragment)
        }
        rightImageButton.setOnClickListener {
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_profile_to_local)
        }
    }

    private fun profileEditToolbar(){
        Log.d(TAG, "Setting Toolbar to ProfileEdit")
        toolbarTitleTV.text = "Edit Profile"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
        rightImageButton.setImageResource(R.drawable.ic_check_white_24dp)
        leftImageButton.setImageResource(R.drawable.ic_clear_white_24dp)
        rightImageButton.visibility = View.VISIBLE
        leftImageButton.visibility = View.VISIBLE
        rightImageButton.setOnClickListener {
            //send changes server side and close edit
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_profileEditFragment_to_profile)
            //todo send update to server and update viewmodel

        }
        leftImageButton.setOnClickListener {
            //close edit
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_profileEditFragment_to_profile)
        }
    }
    private fun roamToolbar(){
        Log.d(TAG, "Setting Toolbar to Roam")
        toolbarTitleTV.text = "Roam"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
    }
    private fun postToolbar(){
        Log.d(TAG, "Setting Toolbar to Post")
        toolbarTitleTV.text = "Post"
        toolbarSendTv.visibility = View.VISIBLE
        toolbarCancelTv.visibility = View.VISIBLE
        rightImageButton.visibility = View.INVISIBLE
        leftImageButton.visibility = View.INVISIBLE

        toolbarSendTv.setOnClickListener {
            //todo send post to server and go back to local, update local list with this post

        }
        toolbarCancelTv.setOnClickListener {
            mActivity.findNavController(R.id.main_nav_host_fragment).navigate(R.id.action_postFragment_to_local)
        }
    }
    private fun postPreviewToolbar(){
        Log.d(TAG, "Setting Toolbar to Post-Preview")
        toolbarTitleTV.text = "Post"
        toolbarSendTv.visibility = View.INVISIBLE
        toolbarCancelTv.visibility = View.INVISIBLE
    }

    private fun showSignInDialog(){
        AlertDialog.Builder(mActivity)
            .setCancelable(false)
            .setTitle("Create Account")
            .setMessage("You must create an account to continue")
            .setPositiveButton("Ok"){dialog, id->
                Toast.makeText(mActivity, "GO TO PHONE NUMBER", Toast.LENGTH_SHORT).show()
                val intent  = Intent(mActivity, LoginActivity::class.java)
                intent.putExtra("phone", true)
                mActivity.startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton("cancel"){dialog, id->
                Toast.makeText(mActivity, "Cancel", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }
            .show()

    }

}